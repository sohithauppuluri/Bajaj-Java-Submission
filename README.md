# Bajaj-Java-Submission

# HRX Hiring Challenge – Java Submission

This repository contains the Java solution for the HRX Hiring Challenge.

---

## Repository Structure

.
├── final-query.txt
├── pom.xml
├── src
│   └── main
│       └── java
│           └── com
│               └── example
│                   └── hrxchallenge
│                       ├── AppConfig.java
│                       └── HrxChallengeApplication.java
└── target
    ├── classes
    │   └── com
    │       └── example
    │           └── hrxchallenge
    │               ├── AppConfig.class
    │               ├── HrxChallengeApplication.class
    │               └── HrxChallengeApplication$GenerateWebhookResponse.class
    ├── generated-sources
    │   └── annotations
    ├── hrx-challenge-1.0.0.jar
    ├── hrx-challenge-1.0.0.jar.original
    ├── maven-archiver
    │   └── pom.properties
    └── maven-status
        └── maven-compiler-plugin
            └── compile
                └── default-compile
                    ├── createdFiles.lst
                    └── inputFiles.lst
---

## How to Run

1. Ensure you have **Java 17+** and **Maven** installed.
2. Build the project and create the JAR:

```bash
mvn clean packag
```
Run the JAR:
```bash
java -jar target/hrx-challenge-1.0.0.jar
```
