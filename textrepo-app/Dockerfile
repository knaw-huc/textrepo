FROM knawhuc/textrepo-builder:latest AS builder

WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM adoptopenjdk/openjdk16:alpine
COPY --from=builder /build/target/textrepo-1.0-SNAPSHOT.jar /textrepo-app/textrepo.jar

ARG DOCKER_TAG
ENV DOCKER_TAG $DOCKER_TAG

ARG SOURCE_COMMIT
ENV SOURCE_COMMIT $SOURCE_COMMIT

WORKDIR /textrepo-app
RUN apk add --no-cache gettext bash
COPY ./config.yml /textrepo-app/
COPY ./scripts/start.sh /textrepo-app/
COPY ./db /textrepo-app/db

CMD ["./start.sh"]
