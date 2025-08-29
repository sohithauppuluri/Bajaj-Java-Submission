## HRX Challenge - Java Submission

### Project Description

This is a Spring Boot application for the HRX hiring challenge. The application:

1. Generates a webhook on startup using candidate details.
2. Determines the SQL query to submit based on the candidate registration number.
3. Saves the SQL query to a local file.
4. Submits the SQL query to the received webhook URL using JWT authentication.

### Repository Structure

```
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
```

### Build & Run Instructions

1. Ensure Java 17+ and Maven are installed.
2. Clone the repository.
3. Run the following commands:

```bash
mvn clean package  # Builds the project and generates the JAR
java -jar target/hrx-challenge-1.0.0.jar  # Runs the application
```

4. The application will automatically submit the SQL query to the webhook.

### Important Files

* `src/main/java/com/example/hrxchallenge/HrxChallengeApplication.java` : Main Spring Boot application.
* `final-query.txt` : Contains the generated SQL query.
* `target/hrx-challenge-1.0.0.jar` : Runnable JAR output.
* `pom.xml` : Maven project file.
* `README.md` : This documentation.

### Submission

* GitHub Repository: \[Your Repo Link Here]
* Public JAR file (downloadable): Also available in github repo
