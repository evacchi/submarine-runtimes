/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jbpm.compiler.canonical;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.CastExpr;
import com.github.javaparser.ast.expr.ClassExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.LambdaExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.StringLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.UnknownType;
import org.drools.core.ruleunit.impl.EntryPointDataProcessor;
import org.drools.core.util.ClassUtils;
import org.drools.core.util.StringUtils;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.ruleflow.core.factory.RuleSetNodeFactory;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.kie.api.definition.process.Node;
import org.kie.kogito.rules.DataObserver;
import org.kie.kogito.rules.DataSource;
import org.kie.kogito.rules.DataStore;
import org.kie.kogito.rules.DataStream;

import static com.github.javaparser.StaticJavaParser.parse;
import static com.github.javaparser.StaticJavaParser.parseClassOrInterfaceType;
import static com.github.javaparser.StaticJavaParser.parseExpression;
import static com.github.javaparser.StaticJavaParser.parseStatement;

public class RuleSetNodeVisitor extends AbstractVisitor {

    private final ClassLoader contextClassLoader;

    public RuleSetNodeVisitor(ClassLoader contextClassLoader) {
        this.contextClassLoader = contextClassLoader;
    }

    @Override
    public void visitNode(String factoryField, Node node, BlockStmt body, VariableScope variableScope, ProcessMetaData metadata) {
        RuleSetNode ruleSetNode = (RuleSetNode) node;

        addFactoryMethodWithArgsWithAssignment(factoryField, body, RuleSetNodeFactory.class, "ruleSetNode" + node.getId(), "ruleSetNode", new LongLiteralExpr(ruleSetNode.getId()));
        addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "name", new StringLiteralExpr(getOrDefault(ruleSetNode.getName(), "Rule")));
        // build supplier for either KieRuntime or DMNRuntime
        BlockStmt actionBody = new BlockStmt();
        LambdaExpr lambda = new LambdaExpr(new Parameter(new UnknownType(), "()"), actionBody);

        RuleSetNode.RuleType ruleType = ruleSetNode.getRuleType();

        if (ruleSetNode.getLanguage().equals(RuleSetNode.DRL_LANG)) {
            MethodCallExpr ruleRuntimeBuilder = new MethodCallExpr(
                    new MethodCallExpr(new NameExpr("app"), "ruleUnits"), "ruleRuntimeBuilder");
            MethodCallExpr ruleRuntimeSupplier = new MethodCallExpr(ruleRuntimeBuilder, "newKieSession", NodeList.nodeList(new StringLiteralExpr("defaultStatelessKieSession"), new NameExpr("app.config().rule()")));
            actionBody.addStatement(new ReturnStmt(ruleRuntimeSupplier));
            addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "ruleFlowGroup", new StringLiteralExpr(ruleType.getName()), lambda);
        } else if (ruleSetNode.getLanguage().equals(RuleSetNode.RULE_UNIT_LANG)) {
            InputStream resourceAsStream = this.getClass().getResourceAsStream("/class-templates/RuleUnitFactoryTemplate.java");
            Expression ruleUnitFactory = parse(resourceAsStream).findFirst(Expression.class).get();

            String unitName = ruleType.getName();
            Class<?> unitClass = loadUnitClass(unitName);

            ruleUnitFactory.findAll(ClassOrInterfaceType.class)
                    .stream()
                    .filter(t -> t.getNameAsString().equals("$Type$"))
                    .forEach(t -> t.setName(unitName));

            ruleUnitFactory.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("bind"))
                    .ifPresent(m -> m.setBody(bind(variableScope, ruleSetNode, unitClass)));
            ruleUnitFactory.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("unit"))
                    .ifPresent(m -> m.setBody(unit(unitName)));
            ruleUnitFactory.findFirst(MethodDeclaration.class, m -> m.getNameAsString().equals("unbind"))
                    .ifPresent(m -> m.setBody(unbind(variableScope, ruleSetNode, unitClass)));

            addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "ruleUnit", new StringLiteralExpr(ruleType.getName()), ruleUnitFactory);
        } else if (ruleSetNode.getLanguage().equals(RuleSetNode.DMN_LANG)) {
            RuleSetNode.RuleType.Decision decisionModel = (RuleSetNode.RuleType.Decision) ruleType;
            MethodCallExpr ruleRuntimeSupplier = new MethodCallExpr(new NameExpr("app"), "dmnRuntimeBuilder");
            actionBody.addStatement(new ReturnStmt(ruleRuntimeSupplier));
            addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "dmnGroup", new StringLiteralExpr(decisionModel.getNamespace()),
                                     new StringLiteralExpr(decisionModel.getModel()),
                                     decisionModel.getDecision() == null ? new NullLiteralExpr() : new StringLiteralExpr(decisionModel.getDecision()),
                                     lambda);
        } else {
            throw new IllegalArgumentException("Unsupported rule language " + ruleSetNode.getLanguage());
        }

        for (Entry<String, String> entry : ruleSetNode.getInMappings().entrySet()) {
            addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "inMapping", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue()));
        }
        for (Entry<String, String> entry : ruleSetNode.getOutMappings().entrySet()) {
            addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "outMapping", new StringLiteralExpr(entry.getKey()), new StringLiteralExpr(entry.getValue()));
        }

        visitMetaData(ruleSetNode.getMetaData(), body, "ruleSetNode" + node.getId());

        addFactoryMethodWithArgs(body, "ruleSetNode" + node.getId(), "done");

    }

    private Class<?> loadUnitClass(String unitName)  {
        try {
        return contextClassLoader.loadClass(unitName);
    } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
    }
    }

    private BlockStmt bind(VariableScope variableScope, RuleSetNode node, Class<?> unitClass) {
        // we need an empty constructor for now
        AssignExpr assignExpr = new AssignExpr(
                new VariableDeclarationExpr(new ClassOrInterfaceType(null, unitClass.getCanonicalName()), "model"),
                new ObjectCreationExpr().setType(unitClass.getCanonicalName()),
                AssignExpr.Operator.ASSIGN);

        BlockStmt actionBody = new BlockStmt();
        actionBody.addStatement(assignExpr);

        // warning: load class here
        for (Map.Entry<String, String> e : node.getInMappings().entrySet()) {
            Variable v = variableScope.findVariable(extractVariableFromExpression(e.getValue()));
            if (v != null) {
                actionBody.addStatement(makeAssignment(v));
                actionBody.addStatement(callSetter(unitClass, "model", e.getKey(), e.getValue()));
            }
        }

        actionBody.addStatement(new ReturnStmt(new NameExpr("model")));
        return actionBody;
    }

    private MethodCallExpr callSetter(Class<?> variableDeclarations, String targetVar, String destField, String value) {
        if (value.startsWith("#{")) {
            value = value.substring(2, value.length() -1);
        }

        return callSetter(variableDeclarations, targetVar, destField, new NameExpr(value));
    }

    private MethodCallExpr callSetter(Class<?> unitClass, String targetVar, String destField, Expression value) {
        String methodName = "get" + StringUtils.capitalize(destField);
        Method m;
        try {
            m = unitClass.getMethod(methodName);
            Expression fieldAccessor =
                    new MethodCallExpr(new NameExpr("model"), methodName);
            if ( DataStore.class.isAssignableFrom(m.getReturnType() ) ) {
                return new MethodCallExpr(fieldAccessor, "add")
                        .addArgument(value);
            } else if ( DataStream.class.isAssignableFrom(m.getReturnType() ) ) {
                return new MethodCallExpr(fieldAccessor, "append")
                        .addArgument(value);
            } // else fallback to the following
        } catch (NoSuchMethodException e) {
            // fallback to the following
        }

        String setter = "set" + StringUtils.capitalize(destField);
        try {
            m = unitClass.getMethod(methodName);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
        Class<?> returnType = m.getReturnType();
        return new MethodCallExpr(new NameExpr(targetVar), setter).addArgument(
                new CastExpr(
                        new ClassOrInterfaceType(null, returnType.getCanonicalName()),
                        new EnclosedExpr(value)));

    }

    private BlockStmt unit(String unitName) {
        MethodCallExpr ruleUnit = new MethodCallExpr(
                new MethodCallExpr(new NameExpr("app"), "ruleUnits"), "create")
                .addArgument(new ClassExpr().setType(unitName));
        return new BlockStmt().addStatement(new ReturnStmt(ruleUnit));
    }

    private BlockStmt unbind(VariableScope variableScope, RuleSetNode node, Class<?> unitClass) {
        BlockStmt stmts = new BlockStmt();

        for (Map.Entry<String, String> e : node.getOutMappings().entrySet()) {
            stmts.addStatement(makeAssignmentFromModel(variableScope.findVariable(e.getValue()), e.getKey(), unitClass));
        }

        return stmts;
    }

    protected Statement makeAssignmentFromModel(Variable v, String name, Class<?> unitClass) {
        String vname = v.getName();
        ClassOrInterfaceType type = parseClassOrInterfaceType(v.getType().getStringType());

        String methodName = "get" + StringUtils.capitalize(name);
        Method m;
        try {
            m = unitClass.getMethod(methodName);
            if ( DataSource.class.isAssignableFrom( m.getReturnType() ) ) {
                Expression fieldAccessor =
                        new MethodCallExpr(new NameExpr("model"), methodName);

                return new ExpressionStmt(new MethodCallExpr(fieldAccessor, "subscribe")
                                                  .addArgument(new MethodCallExpr(
                                                          new NameExpr(DataObserver.class.getCanonicalName()), "of")
                                                                       .addArgument(parseExpression("o -> kcontext.setVariable(\"" + vname + "\", o)"))));
            } // else fallback to the following
        } catch (NoSuchMethodException e) {
            // fallback to the following
        }




        // `type` `name` = (`type`) `model.get<Name>
        BlockStmt blockStmt = new BlockStmt();
        blockStmt.addStatement(new AssignExpr(
                new VariableDeclarationExpr(type, name),
                new CastExpr(
                        type,
                        new MethodCallExpr(
                                new NameExpr("model"),
                                "get" + StringUtils.capitalize(name))),
                AssignExpr.Operator.ASSIGN));
            blockStmt.addStatement(new MethodCallExpr()
                                       .setScope(new NameExpr("kcontext"))
                                       .setName("setVariable")
                                       .addArgument(new StringLiteralExpr(vname))
                                       .addArgument(name));

        return blockStmt;
    }

}
