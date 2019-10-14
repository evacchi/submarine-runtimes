/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.kogito.index.json;

import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.kie.kogito.index.event.KogitoProcessCloudEvent;
import org.kie.kogito.index.model.ProcessInstance;

import static org.kie.kogito.index.Constants.PROCESS_INSTANCES_DOMAIN_ATTRIBUTE;
import static org.kie.kogito.index.json.JsonUtils.getObjectMapper;

public class ProcessInstanceMetaMapper implements Function<KogitoProcessCloudEvent, ObjectNode> {

    @Override
    public ObjectNode apply(KogitoProcessCloudEvent event) {
        if (event == null) {
            return null;
        } else {
            ProcessInstance pi = event.getData();

            ObjectNode json = getObjectMapper().createObjectNode();
            json.put("id", event.getRootProcessInstanceId() == null ? event.getProcessInstanceId() : event.getRootProcessInstanceId());
            json.put("processId", event.getRootProcessId() == null ? event.getProcessId() : event.getRootProcessId());
            json.withArray(PROCESS_INSTANCES_DOMAIN_ATTRIBUTE).add(getProcessJson(event, pi));
            json.setAll((ObjectNode) event.getData().getVariables());
            return json;
        }
    }

    private ObjectNode getProcessJson(KogitoProcessCloudEvent event, ProcessInstance pi) {
        ObjectNode json = getObjectMapper().createObjectNode();
        json.put("id", pi.getId());
        json.put("processId", pi.getProcessId());
        if (pi.getRootProcessInstanceId() != null) {
            json.put("rootProcessInstanceId", pi.getRootProcessInstanceId());
        }
        if (pi.getParentProcessInstanceId() != null) {
            json.put("parentProcessInstanceId", pi.getParentProcessInstanceId());
        }
        if (pi.getRootProcessId() != null) {
            json.put("rootProcessId", pi.getRootProcessId());
        }
        json.put("state", pi.getState());
        if (event.getSource() != null) {
            json.put("endpoint", event.getSource().toString());
        }
        if (pi.getStart() != null) {
            json.put("start", pi.getStart().toInstant().toEpochMilli());
        }
        if (pi.getEnd() != null) {
            json.put("end", pi.getEnd().toInstant().toEpochMilli());
        }
        return json;
    }
}