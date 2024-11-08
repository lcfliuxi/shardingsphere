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

package org.apache.shardingsphere.sharding.route.engine.type.broadcast;

import com.google.common.collect.Sets;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Sharding data source group broadcast route engine.
 */
public final class ShardingDataSourceGroupBroadcastRouteEngine implements ShardingRouteEngine {
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        Collection<Set<String>> broadcastDataSourceGroup = getBroadcastDataSourceGroup(getDataSourceGroup(shardingRule));
        for (Set<String> each : broadcastDataSourceGroup) {
            String dataSourceName = getRandomDataSourceName(each);
            result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.emptyList()));
        }
        return result;
    }
    
    private Collection<Set<String>> getBroadcastDataSourceGroup(final Collection<Set<String>> dataSourceGroup) {
        Collection<Set<String>> result = new LinkedList<>();
        for (Set<String> each : dataSourceGroup) {
            result = getCandidateDataSourceGroup(result, each);
        }
        return result;
    }
    
    private Collection<Set<String>> getDataSourceGroup(final ShardingRule shardingRule) {
        Collection<Set<String>> result = new LinkedList<>();
        for (ShardingTable each : shardingRule.getShardingTables().values()) {
            result.add(each.getDataNodeGroups().keySet());
        }
        return result;
    }
    
    private Collection<Set<String>> getCandidateDataSourceGroup(final Collection<Set<String>> dataSourceSetGroup, final Set<String> compareSet) {
        Collection<Set<String>> result = new LinkedList<>();
        if (dataSourceSetGroup.isEmpty()) {
            result.add(compareSet);
            return result;
        }
        Set<String> intersections;
        boolean hasIntersection = false;
        for (Set<String> each : dataSourceSetGroup) {
            intersections = Sets.intersection(each, compareSet);
            if (intersections.isEmpty()) {
                result.add(each);
            } else {
                result.add(intersections);
                hasIntersection = true;
            }
        }
        if (!hasIntersection) {
            result.add(compareSet);
        }
        return result;
    }
    
    private String getRandomDataSourceName(final Collection<String> dataSourceNames) {
        return new ArrayList<>(dataSourceNames).get(ThreadLocalRandom.current().nextInt(dataSourceNames.size()));
    }
}
