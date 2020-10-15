FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
WORKDIR /indexer
COPY --from=builder /build/target/autocomplete-1.0-SNAPSHOT.jar /indexer/autocomplete.jar
COPY ./config.yml /indexer/
COPY ./autocomplete-mapping.json /indexer/

CMD java -jar /indexer/autocomplete.jar server config.yml
