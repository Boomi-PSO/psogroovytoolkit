# Boomi PSO Groovy Toolkit
Compiled Groovy implementation of PSO Toolkit functionality.
## Overview
This solution is designed to provide PSO toolkit features in a Jar library containing compiled Groovy classes.
## Goal
The Groovy code replaces large parts of the PSO framework previously using multiple branches and various shapes. Its goal is to increase performance and, via the Jar library, make the implementation unmodifiable by customers.

Only developers given access to this project will be able to craete pull requests with change proposals.
## How to build
### Introduction
This project uses Gradle to:
- Compile using groovyc
- Test using Junit
- Measure code coverage usng Jacoco
- Create the Jar file
### Setup
Minimum requirements on the development computer are:
- JDK11 - The initial project was created with the latest Amazon Corretto JDK 11

It is highly recommended to use a Java IDE. A mainstream IDE will provide a Gradle plugin.

**IMPORTANT**: It is the intention to keep this project IDE agnostic. The initial project was created using the Eclipse IDE but all Eclipse related configuration files have been exluded using .gitignore. If another IDE, such as IntelliJ, is used, please modify the .gitignore file to exlude IDE specific files.
### Build
Create a `gradle.properties` file in the project root directory and point to your JDK home, for example:
```
org.gradle.java.home=C:/Program Files/Amazon Corretto/jdk11.0.24_8
```
**NOTE**: `gradle.properties` is ignored by git.

From a command prompt - or an IDE - in the project directory run `gradlew build`

Please refer to the [Gradle User Guide](https://docs.gradle.org/current/userguide/userguide.html) for more information on gradle specifics.
### Main Gradle Build Files
Note the following files of interest in the project_root/build folder:
- libs/PSOGroovyToolkit-x.x.x.jar - Java library
- reports/tests/test/index.html - Junit results
- reports/jacoco/test/html/index.html - Jacoco results
## How to run in a Boomi Process
### Groovy 2.4+
1. Create a _Data Process Shape_ with a _Custom Scripting_ processing step
2. **IMPORTANT**: Set the Language to `Groovy 2.4`
3. Edit the script
4. Import the Grrovy Command class and call the execute method
```
import com.boomi.psotoolkit.core.CreateAuditLog;

new CreateAuditLog(dataContext).execute();
```
### Java 8+
1. Create a _Custom Library_ with the psogroovytoolkit-x.x.x.jar file
2. Deploy the custom library
3. Create a _Data Process Shape_ with a _Custom Scripting_ processing step
## Version History
### V-1
Initial Version supports the following commands:
- CreateAuditLog
- CreateAuditLogNotification
- CreateEnvelope
- CreateNotification
- FilterSortAuditItems
- GetExecDuration
- ParseEnvelope
- SetContext
- SetContextFacade
- SetProcessCallStack
- UpdateTrackedFields

