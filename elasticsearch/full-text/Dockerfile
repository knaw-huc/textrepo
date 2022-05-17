FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM adoptopenjdk/openjdk16:debian-slim
COPY --from=builder /build/target/full-text-1.0-SNAPSHOT.jar /indexer/full-text.jar

WORKDIR /indexer
RUN apt-get update && apt-get install -y gettext bash
COPY ./config-template.yml /indexer/
COPY ./full-text-mapping.json /indexer/
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
