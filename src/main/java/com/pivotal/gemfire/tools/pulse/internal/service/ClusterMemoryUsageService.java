/*
 * Copyright (c) 2010-2015 Pivotal Software, Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you
 * may not use this file except in compliance with the License. You
 * may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License. See accompanying
 * LICENSE file.
 */

package com.pivotal.gemfire.tools.pulse.internal.service;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.pivotal.gemfire.tools.pulse.internal.data.Cluster;
import com.pivotal.gemfire.tools.pulse.internal.data.Repository;

/**
 * Class ClusterMemoryUsageService
 * 
 * This class contains implementations of getting Cluster's overall current
 * memory usage details and its trend over the time.
 * 
 * @since version 7.5
 */
@Component
@Service("ClusterMemoryUsage")
@Scope("singleton")
public class ClusterMemoryUsageService implements PulseService {

  public JSONObject execute(final HttpServletRequest request) throws Exception {

    // get cluster object
    Cluster cluster = Repository.get().getCluster();

    // json object to be sent as response
    JSONObject responseJSON = new JSONObject();
    // clucter's Memory Usage trend added to json response object

    try {
      responseJSON.put("currentMemoryUsage", cluster.getUsedHeapSize());
      responseJSON.put(
          "memoryUsageTrend",
          new JSONArray(cluster
              .getStatisticTrend(Cluster.CLUSTER_STAT_MEMORY_USAGE)));
      // Send json response
      return responseJSON;
    } catch (JSONException e) {
      throw new Exception(e);
    }
  }
}
