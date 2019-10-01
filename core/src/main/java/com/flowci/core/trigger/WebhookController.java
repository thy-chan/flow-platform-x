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

package com.flowci.core.trigger;

import com.flowci.core.common.domain.GitSource;
import com.flowci.core.common.manager.SpringEventManager;
import com.flowci.core.trigger.converter.GitHubConverter;
import com.flowci.core.trigger.converter.GitLabConverter;
import com.flowci.core.trigger.converter.TriggerConverter;
import com.flowci.core.trigger.domain.GitTrigger;
import com.flowci.core.trigger.event.GitHookEvent;
import com.flowci.util.StringHelper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yang
 */
@Log4j2
@RestController
@RequestMapping("/webhooks")
public class WebhookController {

    @Autowired
    private HttpServletRequest request;

    @Autowired
    private TriggerConverter gitHubConverter;

    @Autowired
    private TriggerConverter gitLabConverter;

    @Autowired
    private SpringEventManager eventManager;

    private final Map<GitSource, TriggerConverter> converterMap = new HashMap<>(2);

    @PostConstruct
    public void createMapping() {
        converterMap.put(GitSource.GITHUB, gitHubConverter);
        converterMap.put(GitSource.GITLAB, gitLabConverter);
    }

    @PostMapping("/{name}")
    public void gitTrigger(@PathVariable String name) throws IOException {
        GitSourceWithEvent data = findGitSourceByHeader(request);

        if (Objects.isNull(data)) {
            return;
        }

        Optional<GitTrigger> trigger = converterMap.get(data.source).convert(data.event, request.getInputStream());

        if (!trigger.isPresent()) {
            return;
        }

        log.info("{} trigger received: {}", data.source, trigger.get());
        eventManager.publish(new GitHookEvent(this, name, trigger.get()));
    }

    private GitSourceWithEvent findGitSourceByHeader(HttpServletRequest request) {
        GitSourceWithEvent obj = new GitSourceWithEvent();

        // github
        String event = request.getHeader(GitHubConverter.Header);
        if (StringHelper.hasValue(event)) {
            obj.source = GitSource.GITHUB;
            obj.event = event;
            return obj;
        }

        // gitlab
        event = request.getHeader(GitLabConverter.Header);
        if (StringHelper.hasValue(event)) {
            obj.source = GitSource.GITLAB;
            obj.event = event;
            return obj;
        }

        return null;
    }

    private class GitSourceWithEvent {

        private GitSource source;

        private String event;

    }
}
