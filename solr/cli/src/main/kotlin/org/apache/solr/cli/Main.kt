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
import com.github.ajalt.clikt.core.subcommands
import org.apache.solr.cli.commands.*
import org.apache.solr.cli.commands.zookeeper.*
import org.apache.solr.cli.commands.zookeeper.ListCommand

suspend fun main(args: Array<String>) = SolrCommand()
    .subcommands(
        VersionCommand(), // TODO Validate usage of version command
        StartCommand()
            .subcommands(
                ExampleCommand(),
            ),
        RestartCommand(),
        StopCommand(),
        HealthCheckCommand(),
        StatusCommand(),
        ApiCommand(),
        CreateCommand(),
        DeleteCommand(),
        ConfigCommand(),
        ClusterCommand(),
        ZookeeperCommand()
            .subcommands(
                UploadCommand(),
                DownloadCommand(),
                RemoveCommand(),
                MoveCommand(),
                CopyCommand(),
                ListCommand(),
                MakeRootCommand(),
                UpdateAclsCommand(),
                LinkCommand(),
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
