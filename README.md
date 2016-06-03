# Enterprise Twitter
A project to load feeds for a given Twitter user

# Project health
[![Build Status](https://travis-ci.org/shivangshah/enterprise-twitter.svg?branch=master)](https://travis-ci.org/shivangshah/enterprise-twitter)
[![coverage](https://img.shields.io/codecov/c/github/shivangshah/enterprise-twitter/master.svg)](https://codecov.io/gh/shivangshah/enterprise-twitter)

# Building the application
Gradle wrapper is already included so all needs to be done is: 

`./gradlew clean build`

# Test Reports
Spock test reports can be found here: `build/spock-reports/index.html`
Jacoco coverage report can be found here: `build/reports/jacoco/test/html/index.html`

# Running the application
Once built, being a spring boot application, you can easily run the application as: 

`java -jar build/libs/enterprise-twitter-0.0.1-SNAPSHOT.jar`

As a part of the server start, some basic pre-population of data happens. This pre-population of data
basically includes 10 users with usernames: `username[i]` and password: `password[i]` where `0 < =i < =9`. Similarly for each user, 
100 tweets are randomly added. By default, all users follow each other (meaning `User0` follows `User[i]` where `1 < =i < =9`. Also,
  a user is NEITHER following himself NOR being followed by himself.

# Swagger Support
I have also added support for `Swagger 2.0` using `springfox`. All you have to do once the service is up is:
- Navigate to `http://localhost:8080/swagger-ui.html`
- Expand the `Twitter User Service`
- Multiple apis should show up. Expand to get detailed documentation on the APIs and try it out from the UI itself !
- Additionally, if you are interested in pulling up API contracts (eventually can be used for `Contract Testing`), 
you can navigate here: `http://localhost:8080/api-docs`

# Libraries used
- Spring Actuator - Get built-in capabilities for debug endpoints (such as `/health`, `/info` etc)
- RxJava & Hystrix - For non-blocking, resilient, application flow
- Spring Web - All MVC related
- Gradle - For building purposes
- Spock Test Framework - For BDD test approach
- Springfox-Swagger - Swagger 2.0 support

# List of TODOS: 
- [x] Basic Implementation and workflow working
- [x] Get Elasticsearch to work as an embeddable datasource
- [x] PoC with spring-JPA
- [ ] Add unit test cases (Spock)
- [ ] Add integration test cases (Spock + MockMvc)
- [x] Update Logging
- [x] Exception Handling (handled by spring boot magic & Controller advice !)
- [ ] Update Documentation
- [x] Swagger Support
