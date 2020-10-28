/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.bpmn2.objects;

import org.kie.kogito.internal.runtime.process.WorkItem;
import org.kie.kogito.internal.runtime.process.WorkItemHandler;
import org.kie.kogito.internal.runtime.process.WorkItemManager;

public class ExceptionOnPurposeHandler implements WorkItemHandler {
	
	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        throw new RuntimeException("Thrown on purpose");
	}
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
	}
}
