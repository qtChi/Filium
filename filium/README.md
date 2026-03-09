# Filium

Modern network simulation tool — a spiritual successor to Filius.

## Requirements
- Java 21 (LTS)
- Maven 3.9+

## Run
```zsh
mvn javafx:run
```

## Test + Coverage
```zsh
mvn verify
```
Coverage report: `target/site/jacoco/index.html`

## Package
```zsh
mvn package
java -jar target/filium-1.0.0-SNAPSHOT.jar
```
