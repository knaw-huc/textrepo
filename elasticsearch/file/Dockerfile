FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM adoptopenjdk/openjdk16:debian-slim
COPY --from=builder /build/target/file-1.0-SNAPSHOT.jar /indexer/file.jar

WORKDIR /indexer
RUN apt-get update && apt-get install -y gettext bash
COPY ./config.yml /indexer/
COPY ./file-mapping.json /indexer/
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
