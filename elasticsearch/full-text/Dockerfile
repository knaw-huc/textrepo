FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
COPY --from=builder /build/target/full-text-1.0-SNAPSHOT.jar /indexer/full-text.jar

WORKDIR /indexer
COPY ./config.yml /indexer/
COPY ./full-text-mapping.json /indexer/
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
