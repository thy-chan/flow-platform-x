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

package com.flowci.core.flow;

import com.flowci.core.auth.annotation.Action;
import com.flowci.core.credential.domain.RSAKeyPair;
import com.flowci.core.flow.domain.Flow;
import com.flowci.core.flow.domain.Flow.Status;
import com.flowci.core.flow.domain.FlowAction;
import com.flowci.core.flow.domain.FlowGitTest;
import com.flowci.core.flow.domain.GitSettings;
import com.flowci.core.flow.service.FlowService;
import com.flowci.domain.http.RequestMessage;
import com.flowci.exception.ArgumentException;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author yang
 */
@RestController
@RequestMapping("/flows")
public class FlowController {

    @Autowired
    private FlowService flowService;

    @GetMapping
    @Action(FlowAction.LIST)
    public List<Flow> list() {
        return flowService.list(Status.CONFIRMED);
    }

    @GetMapping(value = "/{name}")
    @Action(FlowAction.GET)
    public Flow get(@PathVariable String name) {
        return flowService.get(name);
    }

    @GetMapping(value = "/{name}/exist")
    @Action(FlowAction.CHECK_NAME)
    public Boolean exist(@PathVariable String name) {
        return flowService.exist(name);
    }

    @PostMapping(value = "/{name}")
    @Action(FlowAction.CREATE)
    public Flow create(@PathVariable String name) {
        return flowService.create(name);
    }

    @PostMapping("/{name}/variables")
    @Action(FlowAction.ADD_VARS)
    public void addVariables(@PathVariable String name, @RequestBody Map<String, String> variables) {
        Flow flow = flowService.get(name);
        flow.getVariables().putAll(variables);
        flowService.update(flow);
    }

    @PostMapping(value = "/{name}/confirm")
    @Action(FlowAction.CONFIRM)
    public Flow confirm(@PathVariable String name, @RequestBody(required = false) GitSettings gitSettings) {
        if (Objects.isNull(gitSettings)) {
            gitSettings = new GitSettings();
        }
        return flowService.confirm(name, gitSettings.getGitUrl(), gitSettings.getCredential());
    }

    @PostMapping("/{name}/yml")
    @Action(FlowAction.SET_YML)
    public void setupYml(@PathVariable String name, @RequestBody RequestMessage<String> body) {
        Flow flow = flowService.get(name);
        byte[] yml = Base64.getDecoder().decode(body.getData());
        flowService.saveYml(flow, new String(yml));
    }

    @GetMapping(value = "/{name}/yml", produces = MediaType.APPLICATION_JSON_VALUE)
    @Action(FlowAction.GET_YML)
    public String getYml(@PathVariable String name) {
        Flow flow = flowService.get(name);
        String yml = flowService.getYml(flow).getRaw();
        return Base64.getEncoder().encodeToString(yml.getBytes());
    }

    @PostMapping(value = "/{name}/git/test")
    @Action(FlowAction.GIT_TEST)
    public void gitTest(@PathVariable String name, @Validated @RequestBody FlowGitTest body) {
        if (body.hasPrivateKey()) {
            flowService.testGitConnection(name, body.getGitUrl(), body.getPrivateKey());
            return;
        }

        if (body.hasCredentialName()) {
            flowService.testGitConnection(name, body.getGitUrl(), body.getCredential());
            return;
        }

        throw new ArgumentException("Credential name or private key must be provided");
    }

    @GetMapping(value = "/{name}/git/branches")
    @Action(FlowAction.LIST_BRANCH)
    public List<String> listGitBranches(@PathVariable String name) {
        return flowService.listGitBranch(name);
    }

    @DeleteMapping("/{name}")
    @Action(FlowAction.DELETE)
    public Flow delete(@PathVariable String name) {
        return flowService.delete(name);
    }

    /**
     * Create credential for flow only
     */
    @PostMapping("/{name}/credentials/rsa")
    @Action(FlowAction.SETUP_CREDENTIAL)
    public String setupRSACredential(@PathVariable String name, @RequestBody RSAKeyPair keyPair) {
        return flowService.setSshRsaCredential(name, keyPair);
    }

    @PostMapping("/{name}/users")
    @Action(FlowAction.ADD_USER)
    public void addUsers(@PathVariable String name, @RequestBody String[] userIds) {
        Flow flow = flowService.get(name);
        flowService.addUsers(flow, userIds);
    }

    @DeleteMapping("/{name}/users")
    @Action(FlowAction.REMOVE_USER)
    public void removeUsers(@PathVariable String name, @RequestBody String[] userIds) {
        Flow flow = flowService.get(name);
        flowService.removeUsers(flow, userIds);
    }

    @GetMapping("/credentials/{name}")
    @Action(FlowAction.LIST_BY_CREDENTIAL)
    public List<Flow> listFlowByCredentials(@PathVariable String name) {
        return flowService.listByCredential(name);
    }
}
