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
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DeprecatedAttributes;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
import org.apache.commons.cli.Options;
import org.apache.solr.common.cloud.SolrZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Supports zk downconfig command in the bin/solr script. */
public class ConfigSetDownloadTool extends ToolBase {
  private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

  private static final Option CONF_NAME_OPTION_NEW = Option.builder("n")
      .longOpt("conf-name")
      .hasArg()
      .argName("NAME")
      .required(false) // should be true, but we have deprecated option as well.
      .desc("Configset name in ZooKeeper.")
      .build();

  private static final Option CONF_NAME_OPTION_DEP = Option.builder()
      .longOpt("confname")
      .hasArg()
      .argName("NAME")
      .deprecated(
          DeprecatedAttributes.builder()
              .setForRemoval(true)
              .setSince("9.8")
              .setDescription("Use --conf-name instead")
              .get())
      .required(false)
      .desc("Configset name in ZooKeeper.")
      .build();

  private static final OptionGroup CONF_NAME_OPTION = new OptionGroup()
      .addOption(CONF_NAME_OPTION_NEW)
      .addOption(CONF_NAME_OPTION_DEP);

  private static final Option CONF_DIR_OPTION_NEW = Option.builder("d")
      .longOpt("conf-dir")
      .hasArg()
      .argName("DIR")
      .required(false) // should be true, but we have deprecated option as well.
      .desc("Local directory with configs.")
      .build();

  public static final Option CONF_DIR_OPTION_DEP = Option.builder()
      .longOpt("confdir")
      .hasArg()
      .argName("NAME")
      .deprecated(
          DeprecatedAttributes.builder()
              .setForRemoval(true)
              .setSince("9.8")
              .setDescription("Use --conf-dir instead")
              .get())
      .required(false)
      .desc("Local directory with configs.")
      .build();

  public static final OptionGroup CONF_DIR_OPTION = new OptionGroup()
      .addOption(CONF_DIR_OPTION_NEW)
      .addOption(CONF_DIR_OPTION_DEP);

  public ConfigSetDownloadTool() {
    this(CLIO.getOutStream());
  }

  public ConfigSetDownloadTool(PrintStream stdout) {
    super(stdout);
  }

  @Override
  public Options getAllOptions() {
    return new Options()
        .addOptionGroup(CONF_NAME_OPTION)
        .addOptionGroup(CONF_DIR_OPTION)
        .addOptionGroup(CommonCLIOptions.SOLR_URL_OPTION_GROUP)
        .addOptionGroup(CommonCLIOptions.ZK_HOST_OPTION_GROUP)
        .addOption(CommonCLIOptions.CREDENTIALS_OPTION);
  }

  @Override
  public String getName() {
    return "downconfig";
  }

  @Override
  public String getUsage() {
    return "bin/solr zk downconfig [-d <DIR>] [-n <NAME>] [-s <HOST>] [-u <credentials>] [-z <HOST>]";
  }

  @Override
  public void runImpl(CommandLine cli) throws Exception {
    SolrCLI.raiseLogLevelUnlessVerbose(cli);
    String zkHost = SolrCLI.getZkHost(cli);

    try (SolrZkClient zkClient = SolrCLI.getSolrZkClient(cli, zkHost)) {
      echoIfVerbose("\nConnecting to ZooKeeper at " + zkHost + " ...", cli);
      String confName = cli.getOptionValue(CONF_NAME_OPTION);
      String confDir = cli.getOptionValue(CONF_DIR_OPTION);

      Path configSetPath = Paths.get(confDir);
      // we try to be nice about having the "conf" in the directory, and we create it if it's not
      // there.
      if (!configSetPath.endsWith("/conf")) {
        configSetPath = Paths.get(configSetPath.toString(), "conf");
      }
      Files.createDirectories(configSetPath);
      echo(
          "Downloading configset "
              + confName
              + " from ZooKeeper at "
              + zkHost
              + " to directory "
              + configSetPath.toAbsolutePath());

      zkClient.downConfig(confName, configSetPath);
    } catch (Exception e) {
      log.error("Could not complete downconfig operation for reason: ", e);
      throw (e);
    }
  }
}
