FROM textrepobuilder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
WORKDIR /textrepo
COPY --from=builder /build/target/textrepo-1.0-SNAPSHOT.jar /textrepo/textrepo.jar
COPY ./hello-world.yml /textrepo/
CMD java -jar /textrepo/textrepo.jar server hello-world.yml
