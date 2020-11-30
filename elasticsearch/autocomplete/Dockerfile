FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
COPY --from=builder /build/target/autocomplete-1.0-SNAPSHOT.jar /indexer/autocomplete.jar

WORKDIR /indexer
RUN apt-get update && apt-get install -y gettext-base
COPY ./config-template.yml /indexer/
COPY ./autocomplete-mapping.json /indexer/
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
