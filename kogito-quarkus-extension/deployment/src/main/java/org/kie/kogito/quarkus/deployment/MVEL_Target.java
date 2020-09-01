/*
 *  Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package org.kie.kogito.quarkus.deployment;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import org.mvel2.MVEL;
import org.mvel2.ParserContext;

@TargetClass(MVEL.class)
@Substitute
public final class MVEL_Target {

    @Substitute
    public static Class analyze(char[] expression, ParserContext ctx) {
        throw new UnsupportedOperationException("Cannot run MVEL#analyze in native mode");
    }

    @Substitute
    public static Class analyze(String expression, ParserContext ctx) {
        throw new UnsupportedOperationException("Cannot run MVEL#analyze in native mode");
    }

    @Substitute
    public static Object executeExpression(final Object compiledExpression, final Object ctx) {
        throw new UnsupportedOperationException("Cannot run MVEL#analyze in native mode");
    }
}
