FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
WORKDIR /indexer
COPY --from=builder /build/target/full-text-1.0-SNAPSHOT.jar /indexer/full-text.jar
COPY ./config.yml /indexer/
COPY ./full-text-mapping.json /indexer/

CMD java -jar /indexer/full-text.jar server config.yml
