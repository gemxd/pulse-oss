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

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.pivotal.gemfire.tools.pulse.internal.controllers.PulseController;

/**
 * Class PulseVersionService
 * 
 * This class contains implementations of getting Pulse Applications Version's
 * details (like version details, build details, source details, etc) from
 * properties file
 * 
 * @since version 7.0.Beta
 */
@Component
@Service("PulseVersion")
@Scope("singleton")
public class PulseVersionService implements PulseService {

  public JSONObject execute(final HttpServletRequest request) throws Exception {

    // json object to be sent as response
    JSONObject responseJSON = new JSONObject();

    try {
      // Response
      responseJSON.put("pulseVersion",
          PulseController.pulseVersion.getPulseVersion());
      responseJSON.put("buildId",
          PulseController.pulseVersion.getPulseBuildId());
      responseJSON.put("buildDate",
          PulseController.pulseVersion.getPulseBuildDate());
      responseJSON.put("sourceDate",
          PulseController.pulseVersion.getPulseSourceDate());
      responseJSON.put("sourceRevision",
          PulseController.pulseVersion.getPulseSourceRevision());
      responseJSON.put("sourceRepository",
          PulseController.pulseVersion.getPulseSourceRepository());
    } catch (JSONException e) {
      throw new Exception(e);
    }
    // Send json response
    return responseJSON;
  }

}
