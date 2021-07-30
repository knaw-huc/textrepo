FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM adoptopenjdk/openjdk16:alpine
COPY --from=builder /build/target/file-1.0-SNAPSHOT.jar /indexer/file.jar

WORKDIR /indexer
RUN apk add --no-cache gettext bash
COPY ./config.yml /indexer/
COPY ./file-mapping.json /indexer/
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
