FROM knawhuc/textrepo-builder:latest AS builder
COPY /scripts /utils
WORKDIR /concordion
COPY . /concordion/

CMD mvn clean test
