FROM alpine:3.12

RUN apk add --no-cache gettext

WORKDIR /about
COPY ./scripts/start.sh /about
COPY ./src /about/src

CMD ["./start.sh"]
