FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
WORKDIR /indexer
COPY --from=builder /build/target/file-1.0-SNAPSHOT.jar /indexer/file.jar
COPY ./config.yml /indexer/
COPY ./file-mapping.json /indexer/
COPY ./scripts/wait-for-it.sh /indexer/
COPY ./scripts/start.sh /indexer/
RUN apt-get update && apt-get install -y gettext-base

CMD java -jar /indexer/file.jar server config.yml
