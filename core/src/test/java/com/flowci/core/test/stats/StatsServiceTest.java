/*
 * Copyright 2019 flow.ci
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

package com.flowci.core.test.stats;

import com.flowci.core.common.helper.DateHelper;
import com.flowci.core.common.helper.ThreadHelper;
import com.flowci.core.job.domain.Job;
import com.flowci.core.job.event.JobStatusChangeEvent;
import com.flowci.core.stats.init.StatsTypeInitializer;
import com.flowci.core.stats.service.StatsService;
import com.flowci.core.stats.domain.StatsItem;
import com.flowci.core.test.SpringScenario;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author yang
 */
public class StatsServiceTest extends SpringScenario {

    @Autowired
    private StatsTypeInitializer statsTypeInitializer;

    @Autowired
    private StatsService statsService;

    @Before
    public void initTypes() {
        statsTypeInitializer.initJobStatsType();
    }

    @Test
    public void should_add_stats_item_when_job_status_changed() {
        Job job = new Job();
        job.setFlowId("123-456");
        job.setCreatedAt(new Date());
        job.setStatus(Job.Status.SUCCESS);

        multicastEvent(new JobStatusChangeEvent(this, job));
        ThreadHelper.sleep(1000);

        StatsItem item = statsService.get(job.getFlowId(), StatsItem.TYPE_JOB_STATUS, DateHelper.toIntDay(new Date()));
        Assert.assertNotNull(item);
        Assert.assertEquals(Job.FINISH_STATUS.size(), item.getCounter().size());

        Assert.assertEquals(new Float(1.0F), item.getCounter().get("SUCCESS"));
        Assert.assertEquals(new Float(0.0F), item.getCounter().get("FAILURE"));
        Assert.assertEquals(new Float(0.0F), item.getCounter().get("CANCELLED"));
        Assert.assertEquals(new Float(0.0F), item.getCounter().get("TIMEOUT"));
    }

    @Test
    public void should_list_stats_by_days() {
        String flowId = "11123123";

        Date yesterday = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        Date today = Date.from(Instant.now());
        Date tomorrow = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        Job job1 = new Job();
        job1.setFlowId(flowId);
        job1.setCreatedAt(yesterday);
        job1.setStatus(Job.Status.SUCCESS);

        Job job2 = new Job();
        job2.setFlowId(flowId);
        job2.setCreatedAt(today);
        job2.setStatus(Job.Status.SUCCESS);

        Job job3 = new Job();
        job3.setFlowId(flowId);
        job3.setCreatedAt(tomorrow);
        job3.setStatus(Job.Status.FAILURE);

        multicastEvent(new JobStatusChangeEvent(this, job1));
        multicastEvent(new JobStatusChangeEvent(this, job2));
        multicastEvent(new JobStatusChangeEvent(this, job3));
        ThreadHelper.sleep(1000);

        int fromDay = DateHelper.toIntDay(yesterday);
        int toDay = DateHelper.toIntDay(tomorrow);
        List<StatsItem> list = statsService.list(flowId, StatsItem.TYPE_JOB_STATUS, fromDay, toDay);
        Assert.assertNotNull(list);
        Assert.assertEquals(3, list.size());
    }
}