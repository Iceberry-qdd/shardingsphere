/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.sharding.rule.changed;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.spi.rule.RuleItemConfigurationChangedProcessor;
import org.apache.shardingsphere.mode.spi.rule.RuleChangedItemType;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableReferenceRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;

import static org.apache.shardingsphere.test.matcher.ShardingSphereAssertionMatchers.deepEqual;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingTableReferenceChangedProcessorTest {
    
    @SuppressWarnings("unchecked")
    private final RuleItemConfigurationChangedProcessor<ShardingRuleConfiguration, ShardingTableReferenceRuleConfiguration> processor = TypedSPILoader.getService(
            RuleItemConfigurationChangedProcessor.class, new RuleChangedItemType("sharding", "binding_tables"));
    
    @Test
    void assertSwapRuleItemConfiguration() {
        ShardingTableReferenceRuleConfiguration actual = processor.swapRuleItemConfiguration(null, "foo_ref:foo_tbl_0,foo_tbl_1");
        assertThat(actual, deepEqual(new ShardingTableReferenceRuleConfiguration("foo_ref", "foo_tbl_0,foo_tbl_1")));
    }
    
    @Test
    void assertFindRuleConfiguration() {
        ShardingRuleConfiguration ruleConfig = mock(ShardingRuleConfiguration.class);
        assertThat(processor.findRuleConfiguration(mockDatabase(ruleConfig)), is(ruleConfig));
    }
    
    private ShardingSphereDatabase mockDatabase(final ShardingRuleConfiguration ruleConfig) {
        ShardingRule rule = mock(ShardingRule.class);
        when(rule.getConfiguration()).thenReturn(ruleConfig);
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
    
    @Test
    void assertChangeRuleItemConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        ShardingTableReferenceRuleConfiguration toBeChangedItemConfig = new ShardingTableReferenceRuleConfiguration("foo_ref", "bar_tbl_0,bar_tbl_1");
        processor.changeRuleItemConfiguration("foo_ref", currentRuleConfig, toBeChangedItemConfig);
        assertThat(currentRuleConfig.getBindingTableGroups().size(), is(1));
        assertThat(new ArrayList<>(currentRuleConfig.getBindingTableGroups()).get(0).getName(), is("foo_ref"));
        assertThat(new ArrayList<>(currentRuleConfig.getBindingTableGroups()).get(0).getReference(), is("bar_tbl_0,bar_tbl_1"));
    }
    
    @Test
    void assertDropRuleItemConfiguration() {
        ShardingRuleConfiguration currentRuleConfig = createCurrentRuleConfiguration();
        processor.dropRuleItemConfiguration("foo_ref", currentRuleConfig);
        assertTrue(currentRuleConfig.getBindingTableGroups().isEmpty());
    }
    
    private ShardingRuleConfiguration createCurrentRuleConfiguration() {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getBindingTableGroups().add(new ShardingTableReferenceRuleConfiguration("foo_ref", "foo_tbl_0,foo_tbl_1"));
        return result;
    }
}
