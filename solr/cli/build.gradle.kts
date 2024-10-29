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
    kotlin("jvm") version "1.9.10"
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.10")
    implementation("com.github.ajalt.clikt:clikt:5.0.1")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    runtimeOnly("org.apache.logging.log4j:log4j-slf4j2-impl")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")

    // Permit multiplatform dependency and it's transitive core dependency
    permitUsedUndeclared("org.jetbrains:annotations")
    permitUsedUndeclared("com.github.ajalt.clikt:clikt-jvm")
    permitUsedUndeclared("com.github.ajalt.clikt:clikt-core-jvm")

    testImplementation(project(":solr:test-framework"))
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("org.apache.solr.cli.MainKt")
}
