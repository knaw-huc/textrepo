FROM knawhuc/textrepo-builder:latest AS builder
WORKDIR /build/
COPY . /build/
RUN mvn clean install

FROM openjdk:11-jre-slim-stretch
WORKDIR /elasticsearch/autocomplete
COPY --from=builder /build/target/autocomplete-1.0-SNAPSHOT.jar /elasticsearch/autocomplete/autocomplete.jar
COPY ./config.yml /elasticsearch/autocomplete/
COPY ./scripts/wait-for-it.sh /utils/

CMD java -jar /elasticsearch/autocomplete/app.jar server config.yml
