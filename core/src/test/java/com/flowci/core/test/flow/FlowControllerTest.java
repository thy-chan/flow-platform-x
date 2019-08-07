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

package com.flowci.core.test.flow;

import com.flowci.core.common.domain.StatusCode;
import com.flowci.core.flow.domain.Flow;
import com.flowci.core.test.MvcMockHelper;
import com.flowci.core.test.SpringScenario;
import com.flowci.domain.http.ResponseMessage;
import com.flowci.util.StringHelper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Base64;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

/**
 * @author yang
 */
public class FlowControllerTest extends SpringScenario {

    @Autowired
    private FlowMockHelper flowMockHelper;

    @Autowired
    private MvcMockHelper mvcMockHelper;

    private final String flowName = "hello_world";

    @Before
    public void createFlowWithYml() throws Exception {
        mockLogin();
        String yml = StringHelper.toString(load("flow.yml"));
        flowMockHelper.create(flowName, yml);
    }

    @Test
    public void should_get_flow_and_yml_then_delete() throws Exception {
        ResponseMessage<Flow> getFlowResponse = mvcMockHelper
                .expectSuccessAndReturnClass(get("/flows/" + flowName), FlowMockHelper.FlowType);

        Assert.assertEquals(StatusCode.OK, getFlowResponse.getCode());
        Assert.assertEquals(flowName, getFlowResponse.getData().getName());

        ResponseMessage<String> responseMessage = mvcMockHelper
                .expectSuccessAndReturnClass(get("/flows/" + flowName + "/yml"), FlowMockHelper.FlowYmlType);
        String ymlResponse = new String(Base64.getDecoder().decode(responseMessage.getData()));
        Assert.assertEquals(StringHelper.toString(load("flow.yml")), ymlResponse);

        ResponseMessage<Flow> deleted = mvcMockHelper
                .expectSuccessAndReturnClass(delete("/flows/" + flowName), FlowMockHelper.FlowType);
        Assert.assertEquals(StatusCode.OK, deleted.getCode());
    }

    @Test
    public void should_list_flows() throws Exception {
        ResponseMessage<List<Flow>> listFlowResponse = mvcMockHelper
                .expectSuccessAndReturnClass(get("/flows"), FlowMockHelper.ListFlowType);

        Assert.assertEquals(StatusCode.OK, listFlowResponse.getCode());
        Assert.assertEquals(1, listFlowResponse.getData().size());
        Assert.assertEquals(flowName, listFlowResponse.getData().get(0).getName());
    }
}
