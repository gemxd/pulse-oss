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

import com.pivotal.gemfire.tools.pulse.internal.controllers.PulseController;
import com.pivotal.gemfire.tools.pulse.internal.data.Cluster;
import com.pivotal.gemfire.tools.pulse.internal.data.PulseConstants;
import com.pivotal.gemfire.tools.pulse.internal.data.Repository;
import com.pivotal.gemfire.tools.pulse.internal.util.StringUtils;

/**
 * Class MemberDetailsService
 * 
 * This class contains implementations of getting Memeber's Statistics.
 * 
 * @since version 7.5
 */
@Component
@Service("MemberDetails")
@Scope("singleton")
public class MemberDetailsService implements PulseService {

  public JSONObject execute(final HttpServletRequest request) throws Exception {

    String userName = request.getUserPrincipal().getName();

    // get cluster object
    Cluster cluster = Repository.get().getCluster();

    // json object to be sent as response
    JSONObject responseJSON = new JSONObject();
    try {

      JSONObject requestDataJSON = new JSONObject(
          request.getParameter("pulseData"));
      String memberName = requestDataJSON.getJSONObject("MemberDetails")
          .getString("memberName");

      responseJSON.put("connectedFlag", cluster.isConnectedFlag());
      responseJSON.put("connectedErrorMsg", cluster.getConnectionErrorMsg());

      Cluster.Member clusterMember = cluster.getMember(StringUtils
          .makeCompliantName(memberName));
      if (clusterMember != null) {
        responseJSON.put("memberId", clusterMember.getId());
        responseJSON.put("name", clusterMember.getName());
        responseJSON.put("host", clusterMember.getHost());
        responseJSON.put("clusterId", cluster.getId());
        responseJSON.put("clusterName", cluster.getServerName());
        responseJSON.put("userName", userName);
        responseJSON.put("loadAverage", clusterMember.getLoadAverage());
        responseJSON.put("sockets", clusterMember.getTotalFileDescriptorOpen());
        responseJSON.put("openFDs", clusterMember.getTotalFileDescriptorOpen());
        responseJSON.put("threads", clusterMember.getNumThreads());
        responseJSON.put("offHeapFreeSize", clusterMember.getOffHeapFreeSize());
        responseJSON.put("offHeapUsedSize", clusterMember.getOffHeapUsedSize());
        responseJSON.put("regionsCount", clusterMember.getMemberRegionsList().length);

        // Number of member clients
        if (PulseController.getPulseProductSupport().equalsIgnoreCase(
            PulseConstants.PRODUCT_NAME_GEMFIREXD)){
          responseJSON.put("numClients", clusterMember.getNumGemFireXDClients());
        }else{
          responseJSON.put("numClients", clusterMember.getMemberClientsHMap().size());
        }

        DecimalFormat df2 = new DecimalFormat(
            PulseConstants.DECIMAL_FORMAT_PATTERN);
        Long diskUsageVal = clusterMember.getTotalDiskUsage();
        Double diskUsage = diskUsageVal.doubleValue() / 1024;

        responseJSON.put("diskStorageUsed",
            Double.valueOf(df2.format(diskUsage)));

        Cluster.Alert[] alertsList = cluster.getAlertsList();

        String status = "Normal";

        for (Cluster.Alert alert : alertsList) {
          if (clusterMember.getName().equals(alert.getMemberName())) {
            if (alert.getSeverity() == Cluster.Alert.SEVERE) {
              status = "Severe";
              break;
            } else if (alert.getSeverity() == Cluster.Alert.ERROR) {
              status = "Error";
            } else if (alert.getSeverity() == Cluster.Alert.WARNING) {
              status = "Warning";
            }
          }
        }

        responseJSON.put("status", status);

      } else {
        responseJSON.put("errorOnMember", "Member [" + memberName
            + "] is not available");
      }

      // Send json response
      return responseJSON;
    } catch (JSONException e) {
      throw new Exception(e);
    }
  }
}
