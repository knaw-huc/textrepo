FROM maven:3.6-jdk-11
WORKDIR /build/
COPY ./pom.xml /build
COPY ./dependency-reduced-pom.xml /build
RUN mvn de.qaware.maven:go-offline-maven-plugin:resolve-dependencies
