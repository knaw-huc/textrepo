FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM adoptopenjdk/openjdk16:alpine
COPY --from=builder /build/target/autocomplete-1.0-SNAPSHOT.jar /indexer/autocomplete.jar

WORKDIR /indexer
RUN apk add --no-cache gettext bash
COPY ./config-template.yml /indexer/
COPY ./autocomplete-mapping.json /indexer/
COPY ./scripts/start.sh /indexer/

CMD ["./start.sh"]
