# SEO Web Page Analyzer

### Tests, linter and maintainability statuses:
[![Actions Status](https://github.com/RassAnDev/java-project-72/workflows/hexlet-check/badge.svg)](https://github.com/RassAnDev/java-project-72/actions)
[![Java CI](https://github.com/RassAnDev/java-project-72/actions/workflows/main.yml/badge.svg)](https://github.com/RassAnDev/java-project-72/actions/workflows/main.yml)
[![Maintainability](https://api.codeclimate.com/v1/badges/d5e737153c152de20da6/maintainability)](https://codeclimate.com/github/RassAnDev/java-project-72/maintainability)
[![Test Coverage](https://api.codeclimate.com/v1/badges/d5e737153c152de20da6/test_coverage)](https://codeclimate.com/github/RassAnDev/java-project-72/test_coverage)

## About
This application is a website that allows you to analyze web pages for suitability for Search Engine Optimization(SEO)

## Requirements:
Before using this application you must install and configure:
* JDK 20;
* Gradle 8.2

## Stack:
|Language and Services |Web Technologies | Data Bases| Tests             | CI, Code Coverage and Reports|
|----------------------|-----------------|-----------|-------------------|------------------------------|
| Java                 | Javalin         | ORM Ebean | JUnit             | GitHub Actions               |
| Gradle               | Thymeleaf       | SQL DB H2 | MockWebServer     | CodeClimate                  |
| Deploy: Render       | Bootstrap       | PosgreSQL | Unirest           | Jacoco                       |

## Setup Application

```bash
make setup
```

## Start Application on localhost

```bash
make start
```

### [An example of a deployed application](https://page-analyzer-f6k7.onrender.com)
