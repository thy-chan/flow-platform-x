package com.flowci.core.test.agent;

import com.flowci.core.agent.domain.AgentHost;
import com.flowci.core.agent.domain.LocalUnixAgentHost;
import com.flowci.core.agent.service.AgentHostService;
import com.flowci.core.agent.service.AgentService;
import com.flowci.core.test.SpringScenario;
import com.flowci.exception.NotAvailableException;
import com.google.common.collect.Sets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class AgentHostServiceTest extends SpringScenario {

    @Autowired
    private AgentService agentService;

    @Autowired
    private AgentHostService agentHostService;

    @Before
    public void login() {
        mockLogin();   
    }

    @Test(expected = NotAvailableException.class)
    public void should_enable_to_create_unix_local_host() {
        // when: create host
        AgentHost host = new LocalUnixAgentHost();
        host.setName("test-host");
        host.setTags(Sets.newHashSet("local", "test"));
        agentHostService.create(host);

        // then:
        Assert.assertNotNull(host.getId());
        Assert.assertEquals(AgentHost.Type.LocalUnixSocket, host.getType());
        Assert.assertEquals(1, agentHostService.list().size());
        Assert.assertEquals(host, agentHostService.list().get(0));

        // when: create other
        AgentHost another = new LocalUnixAgentHost();
        another.setName("test-host-failure");
        another.setTags(Sets.newHashSet("local", "test"));
        agentHostService.create(another);
    }

    @Test
    public void should_start_agents_on_host() {
        AgentHost host = new LocalUnixAgentHost();
        host.setName("test-host");
        host.setTags(Sets.newHashSet("local", "test"));
        agentHostService.create(host);

        // when: start agents on host
        Assert.assertTrue(agentHostService.start(host));
        Assert.assertTrue(agentHostService.start(host));
        Assert.assertTrue(agentHostService.start(host));

        // then:
        Assert.assertEquals(3, agentHostService.size(host));
        Assert.assertEquals(3, agentService.list().size());

        agentHostService.removeAll(host);
        Assert.assertEquals(0, agentHostService.size(host));
        Assert.assertEquals(0, agentService.list().size());
    }

    @Test
    public void should_should_over_time_limit() {
        AgentHost host = new LocalUnixAgentHost();

        // test idle limit
        Instant updatedAt = Instant.now().minus(1, ChronoUnit.HOURS);
        Assert.assertTrue(host.isOverMaxIdleSeconds(Date.from(updatedAt)));

        updatedAt = Instant.now().minus(2, ChronoUnit.SECONDS);
        Assert.assertFalse(host.isOverMaxIdleSeconds(Date.from(updatedAt)));

        host.setMaxIdleSeconds(AgentHost.NoLimit);
        Assert.assertFalse(host.isOverMaxIdleSeconds(Date.from(updatedAt)));

        // test offline limit
        updatedAt = Instant.now().minus(2, ChronoUnit.HOURS);
        Assert.assertTrue(host.isOverMaxOfflineSeconds(Date.from(updatedAt)));

        updatedAt = Instant.now().minus(20, ChronoUnit.MINUTES);
        Assert.assertFalse(host.isOverMaxOfflineSeconds(Date.from(updatedAt)));

        host.setMaxIdleSeconds(AgentHost.NoLimit);
        Assert.assertFalse(host.isOverMaxOfflineSeconds(Date.from(updatedAt)));
    }
}