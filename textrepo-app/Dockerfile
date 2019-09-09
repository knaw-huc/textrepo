FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
WORKDIR /textrepo-app
COPY --from=builder /build/target/textrepo-1.0-SNAPSHOT.jar /textrepo-app/textrepo.jar
COPY ./config.yml /textrepo-app/
COPY ./scripts/wait-for-it.sh /utils/

CMD java -jar /textrepo-app/textrepo.jar server config.yml
