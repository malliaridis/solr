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

package org.apache.solr.cli

import org.apache.solr.cli.commands.snapshot.CreateCommand as SnapshotCreateCommand
import org.apache.solr.cli.commands.snapshot.DeleteCommand as SnapshotDeleteCommand
import org.apache.solr.cli.commands.snapshot.DescribeCommand as SnapshotDescribeCommand
import org.apache.solr.cli.commands.snapshot.ExportCommand as SnapshotExportCommand
import org.apache.solr.cli.commands.snapshot.ListCommand as SnapshotListCommand
import com.github.ajalt.clikt.command.main
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import org.apache.solr.cli.commands.ApiCommand
import org.apache.solr.cli.commands.AssertCommand
import org.apache.solr.cli.commands.ConfigCommand
import org.apache.solr.cli.commands.CreateCommand
import org.apache.solr.cli.commands.DeleteCommand
import org.apache.solr.cli.commands.ExampleCommand
import org.apache.solr.cli.commands.ExportCommand
import org.apache.solr.cli.commands.HealthCheckCommand
import org.apache.solr.cli.commands.PackageCommand
import org.apache.solr.cli.commands.PostCommand
import org.apache.solr.cli.commands.PostLogsCommand
import org.apache.solr.cli.commands.RestartCommand
import org.apache.solr.cli.commands.SnapshotCommand
import org.apache.solr.cli.commands.SolrCommand
import org.apache.solr.cli.commands.StartCommand
import org.apache.solr.cli.commands.StatusCommand
import org.apache.solr.cli.commands.StopCommand
import org.apache.solr.cli.commands.VersionCommand
import org.apache.solr.cli.commands.ZookeeperCommand
import org.apache.solr.cli.commands.zookeeper.CopyCommand
import org.apache.solr.cli.commands.zookeeper.DownConfigCommand
import org.apache.solr.cli.commands.zookeeper.LinkConfigCommand
import org.apache.solr.cli.commands.zookeeper.ListCommand
import org.apache.solr.cli.commands.zookeeper.MakeRootCommand
import org.apache.solr.cli.commands.zookeeper.MoveCommand
import org.apache.solr.cli.commands.zookeeper.RemoveCommand
import org.apache.solr.cli.commands.zookeeper.UpConfigCommand
import org.apache.solr.cli.commands.zookeeper.UpdateAclsCommand

suspend fun main(args: Array<String>) = SolrCommand()
    .subcommands(
        StartCommand()
            .subcommands(
                ExampleCommand(),
                VersionCommand(), // TODO Validate usage of version command
            ),
        RestartCommand(),
        StopCommand(),
        HealthCheckCommand(),
        StatusCommand(),
        ApiCommand(),
        CreateCommand(),
        DeleteCommand(),
        ConfigCommand(),
        ZookeeperCommand()
            .subcommands(
                UpConfigCommand(),
                DownConfigCommand(),
                RemoveCommand(),
                MoveCommand(),
                CopyCommand(),
                ListCommand(),
                MakeRootCommand(),
                UpdateAclsCommand(),
                LinkConfigCommand(),
            ),
        SnapshotCommand()
            .subcommands(
                SnapshotCreateCommand(),
                SnapshotDeleteCommand(),
                SnapshotListCommand(),
                SnapshotDescribeCommand(),
                SnapshotExportCommand(),
            ),
        AssertCommand(),
        ExportCommand(),
        PackageCommand(),
        PostCommand(),
        PostLogsCommand(),
    )
    .main(args)
