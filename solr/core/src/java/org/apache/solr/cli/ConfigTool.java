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

package org.apache.solr.cli;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.common.util.NamedList;
import org.noggit.CharArr;
import org.noggit.JSONWriter;

/**
 * Supports config command in the bin/solr script.
 *
 * <p>Sends a POST to the Config API to perform a specified action.
 */
public class ConfigTool extends ToolBase {

  private static final Option COLLECTION_NAME_OPTION = Option.builder("c")
      .longOpt("name")
      .argName("NAME")
      .hasArg()
      .required(true)
      .desc("Name of the collection.")
      .build();

  private static final Option ACTION_OPTION = Option.builder("a")
      .longOpt("action")
      .argName("ACTION")
      .hasArg()
      .required(false)
      .desc("Config API action, one of: set-property, unset-property, set-user-property, unset-user-property; default is 'set-property'.")
      .build();

  private static final Option PROPERTY_OPTION_NEW = Option.builder()
      .longOpt("property")
      .argName("PROP")
      .hasArg()
      .required(false) // Should be TRUE but have a deprecated option to deal with first,
                       // so we enforce in code
      .desc("Name of the Config API property to apply the action to, such as: 'updateHandler.autoSoftCommit.maxTime'.")
      .build();

  private static final Option PROPERTY_OPTION_DEP = Option.builder("p")
      .deprecated(
          DeprecatedAttributes.builder()
              .setForRemoval(true)
              .setSince("9.8")
              .setDescription("Use --property instead")
              .get())
      .hasArg()
      .argName("PROP")
      .required(false)
      .desc("Name of the Config API property to apply the action to, such as: 'updateHandler.autoSoftCommit.maxTime'.")
      .build();

  private static final OptionGroup PROPERTY_OPTION = new OptionGroup()
      .addOption(PROPERTY_OPTION_NEW)
      .addOption(PROPERTY_OPTION_DEP);

  private static final Option VALUE_OPTION = Option.builder("v")
      .longOpt("value")
      .argName("VALUE")
      .hasArg()
      .required(false)
      .desc("Set the property to this value; accepts JSON objects and strings.")
      .build();

  static {
    PROPERTY_OPTION.setRequired(true);
  }

  public ConfigTool() {
    this(CLIO.getOutStream());
  }

  public ConfigTool(PrintStream stdout) {
    super(stdout);
  }

  @Override
  public String getName() {
    return "config";
  }

  @Override
  public Options getAllOptions() {
    return super.getAllOptions()
        .addOption(COLLECTION_NAME_OPTION)
        .addOption(ACTION_OPTION)
        .addOptionGroup(PROPERTY_OPTION)
        .addOption(VALUE_OPTION)
        .addOption(CommonCLIOptions.SOLR_URL_OPTION)
        .addOption(CommonCLIOptions.ZK_HOST_OPTION)
        .addOption(CommonCLIOptions.CREDENTIALS_OPTION);
  }

  @Override
  public void runImpl(CommandLine cli) throws Exception {
    String solrUrl = SolrCLI.normalizeSolrUrl(cli);
    String action = cli.getOptionValue(ACTION_OPTION, "set-property");
    String collection = cli.getOptionValue(COLLECTION_NAME_OPTION);
    String property = cli.getOptionValue(PROPERTY_OPTION);
    String value = cli.getOptionValue(VALUE_OPTION);

    Map<String, Object> jsonObj = new HashMap<>();
    if (value != null) {
      Map<String, String> setMap = new HashMap<>();
      setMap.put(property, value);
      jsonObj.put(action, setMap);
    } else {
      jsonObj.put(action, property);
    }

    CharArr arr = new CharArr();
    (new JSONWriter(arr, 0)).write(jsonObj);
    String jsonBody = arr.toString();

    String updatePath = "/" + collection + "/config";

    echo("\nPOSTing request to Config API: " + solrUrl + updatePath);
    echo(jsonBody);

    try (SolrClient solrClient =
        SolrCLI.getSolrClient(solrUrl, cli.getOptionValue(CommonCLIOptions.CREDENTIALS_OPTION))) {
      NamedList<Object> result = SolrCLI.postJsonToSolr(solrClient, updatePath, jsonBody);
      Integer statusCode = (Integer) result.findRecursive("responseHeader", "status");
      if (statusCode == 0) {
        if (value != null) {
          echo("Successfully " + action + " " + property + " to " + value);
        } else {
          echo("Successfully " + action + " " + property);
        }
      } else {
        throw new Exception("Failed to " + action + " property due to:\n" + result);
      }
    }
  }
}
