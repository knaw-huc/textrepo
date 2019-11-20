FROM knawhuc/textrepo-builder:latest
COPY /scripts /utils
WORKDIR /concordion
COPY . /concordion/
RUN echo 'hallo build?'
RUN grep -R fileSha /concordion
