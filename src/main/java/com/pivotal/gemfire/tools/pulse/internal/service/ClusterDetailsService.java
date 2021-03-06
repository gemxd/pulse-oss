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

import java.text.DecimalFormat;

import javax.servlet.http.HttpServletRequest;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.pivotal.gemfire.tools.pulse.internal.data.Cluster;
import com.pivotal.gemfire.tools.pulse.internal.data.PulseConstants;
import com.pivotal.gemfire.tools.pulse.internal.data.Repository;

/**
 * Class ClusterDetailsService
 * 
 * This service class has implementation for providing cluster's basic
 * statistical data.
 * 
 * @since version 7.5
 */
@Component
@Service("ClusterDetails")
@Scope("singleton")
public class ClusterDetailsService implements PulseService {

  public JSONObject execute(final HttpServletRequest request) throws Exception {

    String userName = request.getUserPrincipal().getName();

    // get cluster object
    Cluster cluster = Repository.get().getCluster();

    // json object to be sent as response
    JSONObject responseJSON = new JSONObject();

    Cluster.Alert[] alertsList = cluster.getAlertsList();
    int severeAlertCount = 0;
    int errorAlertCount = 0;
    int warningAlertCount = 0;
    int infoAlertCount = 0;

    for (Cluster.Alert alertObj : alertsList) {
      if (alertObj.getSeverity() == Cluster.Alert.SEVERE) {
        severeAlertCount++;
      } else if (alertObj.getSeverity() == Cluster.Alert.ERROR) {
        errorAlertCount++;
      } else if (alertObj.getSeverity() == Cluster.Alert.WARNING) {
        warningAlertCount++;
      } else {
        infoAlertCount++;
      }
    }
    try {
      // getting basic details of Cluster
      responseJSON.put("clusterName", cluster.getServerName());
      responseJSON.put("severeAlertCount", severeAlertCount);
      responseJSON.put("errorAlertCount", errorAlertCount);
      responseJSON.put("warningAlertCount", warningAlertCount);
      responseJSON.put("infoAlertCount", infoAlertCount);

      responseJSON.put("totalMembers", cluster.getMemberCount());
      responseJSON.put("servers", cluster.getServerCount());
      responseJSON.put("clients", cluster.getClientConnectionCount());
      responseJSON.put("locators", cluster.getLocatorCount());
      responseJSON.put("totalRegions", cluster.getTotalRegionCount());
      Long heapSize = cluster.getTotalHeapSize();

      DecimalFormat df2 = new DecimalFormat(
          PulseConstants.DECIMAL_FORMAT_PATTERN);
      Double heapS = heapSize.doubleValue() / 1024;
      responseJSON.put("totalHeap", Double.valueOf(df2.format(heapS)));
      responseJSON.put("functions", cluster.getRunningFunctionCount());
      responseJSON.put("uniqueCQs", cluster.getRegisteredCQCount());
      responseJSON.put("subscriptions", cluster.getSubscriptionCount());
      responseJSON.put("txnCommitted", cluster.getTxnCommittedCount());
      responseJSON.put("txnRollback", cluster.getTxnRollbackCount());
      responseJSON.put("userName", userName);
      responseJSON.put("connectedFlag", cluster.isConnectedFlag());
      responseJSON.put("connectedErrorMsg", cluster.getConnectionErrorMsg());

      return responseJSON;
    } catch (JSONException e) {
      throw new Exception(e);
    }
  }
}
