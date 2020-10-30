FROM knawhuc/textrepo-builder:latest

WORKDIR /concordion
COPY ./src /concordion/src
COPY ./pom.xml /concordion/pom.xml
COPY ./scripts/start.sh /concordion/

CMD ["./start.sh"]
