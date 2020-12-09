/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jbpm.process.instance;

import java.util.Collection;
import java.util.Map;

import org.drools.core.common.EndOperationListener;
import org.drools.core.common.WorkingMemoryAction;
import org.drools.core.impl.EnvironmentImpl;
import org.drools.core.time.TimerService;
import org.drools.kogito.core.common.InternalKnowledgeRuntime;
import org.jbpm.workflow.instance.impl.CodegenNodeInstanceFactoryRegistry;
import org.jbpm.workflow.instance.impl.NodeInstanceFactoryRegistry;
import org.kie.kogito.internal.KieBase;
import org.kie.kogito.internal.event.process.ProcessEventListener;
import org.kie.kogito.internal.event.rule.AgendaEventListener;
import org.kie.kogito.internal.event.rule.RuleRuntimeEventListener;
import org.kie.kogito.internal.logger.KieRuntimeLogger;
import org.kie.kogito.internal.runtime.Calendars;
import org.kie.kogito.internal.runtime.Channel;
import org.kie.kogito.internal.runtime.Environment;
import org.kie.kogito.internal.runtime.Globals;
import org.kie.kogito.internal.runtime.KieSessionConfiguration;
import org.kie.kogito.internal.runtime.ObjectFilter;
import org.kie.kogito.internal.runtime.process.ProcessInstance;
import org.kie.kogito.internal.runtime.process.WorkItemManager;
import org.kie.kogito.internal.runtime.rule.Agenda;
import org.kie.kogito.internal.runtime.rule.AgendaFilter;
import org.kie.kogito.internal.runtime.rule.EntryPoint;
import org.kie.kogito.internal.runtime.rule.FactHandle;
import org.kie.kogito.internal.runtime.rule.LiveQuery;
import org.kie.kogito.internal.runtime.rule.QueryResults;
import org.kie.kogito.internal.runtime.rule.ViewChangedEventListener;
import org.kie.kogito.internal.time.SessionClock;
import org.kie.kogito.jobs.JobsService;

/**
 * A severely limited implementation of the WorkingMemory interface.
 * It only exists for legacy reasons.
 */
class DummyKnowledgeRuntime implements InternalKnowledgeRuntime {

    private final Environment environment;
    private InternalProcessRuntime processRuntime;

    DummyKnowledgeRuntime(InternalProcessRuntime processRuntime) {
        this.processRuntime = processRuntime;
        this.environment = new Environment() {
            EnvironmentImpl originalEnv = new EnvironmentImpl();
            private NodeInstanceFactoryRegistry codegenNodeInstanceFactoryRegistry = new CodegenNodeInstanceFactoryRegistry();

            @Override
            public Object get(String identifier) {
                if (identifier.equals("NodeInstanceFactoryRegistry")) {
                    return codegenNodeInstanceFactoryRegistry;
                } else {
                    return originalEnv.get(identifier);
                }
            }

            @Override
            public void set(String identifier, Object object) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void setDelegate(Environment delegate) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public Agenda getAgenda() {
        return null;
    }

    @Override
    public void setIdentifier(long id) {

    }

    @Override
    public void setEndOperationListener(EndOperationListener listener) {

    }

    @Override
    public long getLastIdleTimestamp() {
        return 0;
    }

    @Override
    public void queueWorkingMemoryAction(WorkingMemoryAction action) {

    }

    @Override
    public InternalProcessRuntime getProcessRuntime() {
        return this.processRuntime;
    }

    @Override
    public Environment getEnvironment() {
        return environment;
    }

    @Override
    public JobsService getJobsService() {
        return null;
    }

    @Override
    public void startOperation() {

    }

    @Override
    public void endOperation() {

    }

    @Override
    public void executeQueuedActions() {

    }

    @Override
    public <T extends SessionClock> T getSessionClock() {
        return null;
    }

    @Override
    public void setGlobal(String identifier, Object value) {

    }

    @Override
    public Object getGlobal(String identifier) {
        return null;
    }

    @Override
    public Globals getGlobals() {
        return null;
    }

    @Override
    public Calendars getCalendars() {
        return null;
    }

    @Override
    public KieBase getKieBase() {
        return null;
    }

    @Override
    public void registerChannel(String name, Channel channel) {

    }

    @Override
    public void unregisterChannel(String name) {

    }

    @Override
    public Map<String, Channel> getChannels() {
        return null;
    }

    @Override
    public KieSessionConfiguration getSessionConfiguration() {
        return null;
    }

    @Override
    public KieRuntimeLogger getLogger() {
        return null;
    }

    @Override
    public void addEventListener(ProcessEventListener listener) {

    }

    @Override
    public void removeEventListener(ProcessEventListener listener) {

    }

    @Override
    public Collection<ProcessEventListener> getProcessEventListeners() {
        return null;
    }

    @Override
    public void addEventListener(RuleRuntimeEventListener listener) {

    }

    @Override
    public void removeEventListener(RuleRuntimeEventListener listener) {

    }

    @Override
    public Collection<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return null;
    }

    @Override
    public void addEventListener(AgendaEventListener listener) {

    }

    @Override
    public void removeEventListener(AgendaEventListener listener) {

    }

    @Override
    public Collection<AgendaEventListener> getAgendaEventListeners() {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String processId) {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public ProcessInstance startProcess( String processId, AgendaFilter agendaFilter ) {
        return null;
    }

    @Override
    public ProcessInstance startProcess(String processId, Map<String, Object> parameters, AgendaFilter agendaFilter) {
        return null;
    }

    @Override
    public ProcessInstance createProcessInstance(String processId, Map<String, Object> parameters) {
        return null;
    }

    @Override
    public ProcessInstance startProcessInstance(String processInstanceId) {
        return null;
    }

    @Override
    public ProcessInstance startProcessInstance(String processInstanceId, String trigger) {
        return null;
    }

    @Override
    public void signalEvent(String type, Object event) {

    }

    @Override
    public void signalEvent(String type, Object event, String processInstanceId) {

    }

    @Override
    public Collection<ProcessInstance> getProcessInstances() {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return null;
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId, boolean readonly) {
        return null;
    }

    @Override
    public void abortProcessInstance(String processInstanceId) {

    }

    @Override
    public WorkItemManager getWorkItemManager() {
        return this.processRuntime.getWorkItemManager();
    }

    @Override
    public void halt() {

    }

    @Override
    public EntryPoint getEntryPoint(String name) {
        return null;
    }

    @Override
    public Collection<? extends EntryPoint> getEntryPoints() {
        return null;
    }

    @Override
    public QueryResults getQueryResults(String query, Object... arguments) {
        return null;
    }

    @Override
    public LiveQuery openLiveQuery(String query, Object[] arguments, ViewChangedEventListener listener) {
        return null;
    }

    @Override
    public String getEntryPointId() {
        return null;
    }

    @Override
    public FactHandle insert(Object object) {
        return null;
    }

    @Override
    public void retract(FactHandle handle) {

    }

    @Override
    public void delete(FactHandle handle) {

    }

    @Override
    public void delete(FactHandle handle, FactHandle.State fhState) {

    }

    @Override
    public void update(FactHandle handle, Object object) {

    }

    @Override
    public void update(FactHandle handle, Object object, String... modifiedProperties) {

    }

    @Override
    public FactHandle getFactHandle(Object object) {
        return null;
    }

    @Override
    public Object getObject(FactHandle factHandle) {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects() {
        return null;
    }

    @Override
    public Collection<? extends Object> getObjects(ObjectFilter filter) {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles() {
        return null;
    }

    @Override
    public <T extends FactHandle> Collection<T> getFactHandles(ObjectFilter filter) {
        return null;
    }

    @Override
    public long getFactCount() {
        return 0;
    }

    @Override
    public TimerService getTimerService() {
        return null;
    }
}