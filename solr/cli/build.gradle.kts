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

plugins {
    kotlin("jvm") version libs.versions.kotlin.get()
    kotlin("plugin.serialization") version libs.versions.kotlin.get()
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Dependencies to solrj required for executing ZK operations
    implementation(project(":solr:core"))
    implementation(project(":solr:solrj"))
    implementation(project(":solr:solrj-zookeeper"))

    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.io.core)
    implementation(libs.ajalt.clikt)
    implementation(libs.oshai.logging.jvm)
    runtimeOnly(libs.apache.log4j.slf4j2impl)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime.jvm)
    implementation(libs.kotlinx.serialization.json)

    implementation(platform(libs.eclipse.jetty.bom))
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.jetty)
    implementation(libs.ktor.client.contentnegotiation)
    implementation(libs.ktor.serialization.json)

    // Permit multiplatform dependency and it's transitive core dependency
    permitUsedUndeclared(libs.jetbrains.annotations)
    permitUsedUndeclared(libs.ajalt.clikt.jvm)
    permitUsedUndeclared(libs.ajalt.clikt.core.jvm)

    testImplementation(platform(libs.junit.bom))
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
}

application {
    applicationName = "solr"
    mainClass.set("org.apache.solr.cli.MainKt")
}
