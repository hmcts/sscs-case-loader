# SSCS Case Loader Application

[![Build Status](https://travis-ci.org/hmcts/spring-boot-template.svg?branch=master)](https://travis-ci.org/hmcts/spring-boot-template)
[![codecov](https://codecov.io/gh/hmcts/sscs-case-loader/branch/master/graph/badge.svg)](https://codecov.io/gh/hmcts/sscs-case-loader)
[![Codacy Badge](https://api.codacy.com/project/badge/Grade/9717adfc9bca44ab98e836c97f28659f)](https://www.codacy.com/app/HMCTS/sscs-case-loader)
[![Codacy Badge](https://api.codacy.com/project/badge/Coverage/9717adfc9bca44ab98e836c97f28659f)](https://www.codacy.com/app/HMCTS/sscs-case-loader)

## Purpose

The purpose of this App is to take cases coming from the legacy Gaps2 and create/update them in the CCD app.

## What's inside

It contains:
 * common plugins and libraries
 * docker setup
 * swagger configuration for api documentation ([see how to publish your api documentation to shared repository](https://github.com/hmcts/reform-api-docs#publish-swagger-docs))
 * code quality tools already set up
 * integration with Travis CI
 * Hystrix circuit breaker enabled
 * Hystrix dashboard
 * MIT license and contribution information

The application exposes health endpoint (http://localhost:8082/health) and metrics endpoint
(http://localhost:8082/metrics).

## Plugins

The project contains the following plugins:

  * checkstyle

    https://docs.gradle.org/current/userguide/checkstyle_plugin.html

    Performs code style checks on Java source files using Checkstyle and generates reports from these checks.
    The checks are included in gradle's *check* task (you can run them by executing `./gradlew check` command).

  * pmd

    https://docs.gradle.org/current/userguide/pmd_plugin.html

    Performs static code analysis to finds common programming flaws. Incuded in gradle `check` task.


  * jacoco

    https://docs.gradle.org/current/userguide/jacoco_plugin.html

    Provides code coverage metrics for Java code via integration with JaCoCo.
    You can create the report by running the following command:

    ```bash
      ./gradlew jacocoTestReport
    ```

    The report will be created in build/reports subdirectory in your project directory.

  * io.spring.dependency-management

    https://github.com/spring-gradle-plugins/dependency-management-plugin

    Provides Maven-like dependency management. Allows you to declare dependency management
    using `dependency 'groupId:artifactId:version'`
    or `dependency group:'group', name:'name', version:version'`.

  * org.springframework.boot

    http://projects.spring.io/spring-boot/

    Reduces the amount of work needed to create a Spring application

  * org.owasp.dependencycheck

    https://jeremylong.github.io/DependencyCheck/dependency-check-gradle/index.html

    Provides monitoring of the project's dependent libraries and creating a report
    of known vulnerable components that are included in the build. To run it
    execute `gradle dependencyCheck` command.

  * com.github.ben-manes.versions

    https://github.com/ben-manes/gradle-versions-plugin

    Provides a task to determine which dependencies have updates. Usage:

    ```bash
      ./gradlew dependencyUpdates -Drevision=release
    ```

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Run the application by executing:

```
  ./gradlew bootRun
```

### Running the application in Docker

Dockerisation is a work in progress.

### Setting up a Dockerised SFTP server for developing purpose

This setup is required if files need to be processed via sftp server

* To simply build the SFTP server:
```bash
docker-compose rm -f && docker-compose -f docker-compose-sftp.yml build && docker-compose -f docker-compose-sftp.yml up
```

* To login into a container which is currently running on your system and view transferred files

Place file to be transferred under `docker/sftp/data/incoming` and make sure `docker/sftp/data/incoming/processed` folder should contain atleast one file and `docker/sftp/data/incoming/failed` should be empty then structure should look like that:

```
sscs-case-loader/
├─ docker/
│  ├─ sftp/
│  │  ├─ data/
│  │  │  ├─ incoming/
│  │  │  │  ├─ processed/
│  │  │  │  │  ├─ <something>.xml
│  │  │  │  ├─ failed/
│  │  │  │  ├─ <something>.xml
```
After running the case-loader job, you should not see any errors and files should land in **processed** directory.


```
To connect into sftp container from sscs-case-loader container use:
```bash
sftp -P 22 -o StrictHostKeyChecking=no -i /home/webapp/sscs-sftp-key sftp@sscs-sftp:incoming
```

To connect into sftp container from host (your computer):
Before first use
```bash
chmod 600 ./docker/sftp-docker
```
and put some files in here
```bash
./docker/sftp/data/incoming/
```
finally
```bash
sftp -P 2222 -o StrictHostKeyChecking=no -i ./docker/sftp-docker sftp@localhost:incoming
```

```bash
Connected to sscs-sftp.
Changing to: /incoming
sftp> dir
SSCS_Extract_Reference_2017-06-30-09-01-31.xml
```
## Running e2e locally

* Bring up the upstream systems using sscs-docker (https://github.com/hmcts/sscs-docker) project.
Please follow the instructions given in the README document.

* Run the application using the "local" profile:
```bash
SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
```

* Turn on debugging by editing sscs-case-loader/src/main/resources/application.yaml
```bash
logging.level:
    org.springframework.web: ${LOG_LEVEL_SPRING_WEB:debug}
    uk.gov.hmcts.reform.sscs: ${LOG_LEVEL_SSCS:debug}
```
* Bring up the SFTP server
```bash
docker-compose rm -f && docker-compose -f docker-compose-sftp.yml build && docker-compose -f docker-compose-sftp.yml up
```
* Open IntelliJ and import the Lombok plugin and enable annotation processing
* Run this test within IntelliJ
```bash
https://github.com/hmcts/sscs-case-loader/blob/master/src/e2e/java/uk.gov.hmcts.reform.sscs/olde2e/ProcessFileAndSaveIntoCcd.java#L20
```
* Refresh the browser to view the cases in CCD
```bash
http://localhost:3451
```

* Running  Functional test locally

1. Make sure you bring up local SFTP and run sscs-docker dependencies
2. Run

```
 SPRING_PROFILES_ACTIVE=local ./gradlew functionalPreDeploy
 SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
 SPRING_PROFILES_ACTIVE=local ./gradlew functionalPostDeploy
```

## Hystrix

[Hystrix](https://github.com/Netflix/Hystrix/wiki) is a library that helps you control the interactions
between your application and other services by adding latency tolerance and fault tolerance logic. It does this
by isolating points of access between the services, stopping cascading failures across them,
and providing fallback options. We recommend you to use Hystrix in your application if it calls any services.

### Hystrix circuit breaker

This template API has [Hystrix Circuit Breaker](https://github.com/Netflix/Hystrix/wiki/How-it-Works#circuit-breaker)
already enabled. It monitors and manages all the`@HystrixCommand` or `HystrixObservableCommand` annotated methods
inside `@Component` or `@Service` annotated classes.

### Hystrix dashboard

When this API is running, you can monitor Hystrix metrics in real time using
[Hystrix Dashboard](https://github.com/Netflix/Hystrix/wiki/Dashboard).
In order to do this, visit http://localhost:4550/hystrix and provide http://localhost:4550/hystrix.stream
as the Hystrix event stream URL. Keep in mind that you'll only see data once some
of your Hystrix commands have been executed. Otherwise *'Loading...'* message will be displayed
on the monitoring page.

### Other

Hystrix offers much more than Circuit Breaker pattern implementation or command monitoring.
Here are some other functionalities it provides:
 * [Separate, per-dependency thread pools](https://github.com/Netflix/Hystrix/wiki/How-it-Works#isolation)
 * [Semaphores](https://github.com/Netflix/Hystrix/wiki/How-it-Works#semaphores), which you can use to limit
 the number of concurrent calls to any given dependency
 * [Request caching](https://github.com/Netflix/Hystrix/wiki/How-it-Works#request-caching), allowing
 different code paths to execute Hystrix Commands without worrying about duplicating work

## Troubleshooting

### IDE Settings

#### Project Lombok Plugin
When building the project in your IDE (eclipse or IntelliJ), Lombok plugin will be required to compile.

For IntelliJ IDEA, please add the Lombok IntelliJ plugin:
* Go to `File > Settings > Plugins`
* Click on `Browse repositories...`
* Search for `Lombok Plugin`
* Click on `Install plugin`
* Restart IntelliJ IDEA

Plugin setup for other IDE's are available on [https://projectlombok.org/setup/overview]

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details


