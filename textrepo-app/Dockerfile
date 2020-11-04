FROM knawhuc/textrepo-builder:latest AS builder

WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
COPY --from=builder /build/target/textrepo-1.0-SNAPSHOT.jar /textrepo-app/textrepo.jar

ARG DOCKER_TAG
ENV DOCKER_TAG $DOCKER_TAG

ARG SOURCE_COMMIT
ENV SOURCE_COMMIT $SOURCE_COMMIT

RUN apt-get update && apt-get install -y gettext-base

WORKDIR /textrepo-app
COPY ./config.yml /textrepo-app/
COPY ./scripts/start.sh /textrepo-app/

CMD ["./start.sh"]
