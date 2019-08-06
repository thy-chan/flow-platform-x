/*
 * Copyright 2018 flow.ci
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flowci.core.agent.consumer;

import com.flowci.core.agent.event.AgentStatusChangeEvent;
import com.flowci.core.common.domain.PushEvent;
import com.flowci.core.common.manager.SocketPushManager;
import com.flowci.domain.Agent;
import com.flowci.domain.Agent.Status;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author yang
 */
@Log4j2
@Component
public class OnAgentStatusChange implements ApplicationListener<AgentStatusChangeEvent> {

    @Autowired
    private SocketPushManager socketPushManager;

    @Autowired
    private String topicForAgents;

    @Override
    public void onApplicationEvent(AgentStatusChangeEvent event) {
        Agent agent = event.getAgent();
        socketPushManager.push(topicForAgents, PushEvent.STATUS_CHANGE, agent);

        String name = agent.getName();
        Status status = agent.getStatus();
        String jobId = agent.getJobId();
        log.debug("Agent {} with status {} for job {} been pushed", name, status, jobId);
    }
}