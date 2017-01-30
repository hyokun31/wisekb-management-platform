# Exobrain WiseKB Management Platform

## Introduction

WiseKB Management Platform is a resource management framework for Exobrain WiseKB.
WiseKB Management Platform drastically simplifies management of Apache UIMA & Hadoop EcoSystem through the following features.

## Feature

* Resource Management Framework
* Oozie Workflow Management
* Oozie Coordinator Management
* Oozie Bundle Management
* HDFS Browser
* YARN Monitoring

## License

[![Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-brightgreen.svg)](LICENSE)

## License Headers

### Java Header

```java
/*
 * Copyright (C) 2012-2016 the Exobrain WiseKB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
```

## For Developer

### Spring Framework Profiles

* Development Mode : -Dspring.profiles.active=development
  Property Place Holder : /WEB-INF/config-dev.properties
* Production Mode : -Dspring.profiles.active=production
  Property Place Holder : /WEB-INF/config.properties

### Site Deployment

Open your Maven settings (~/.m2/settings.xml) and add the following server configuration:

```xml
<settings>
    <servers>
        <server>
            <id>github</id>
            <username>YOUR_GITHUB_USERNAME</username>
            <password>YOUR_PASSWORD</password>
        </server>
    </servers>
</settings>
```

Use following command for deploying site :

```
# mvn site:site
# mvn site:stage
# mvn site:deploy
```
