FROM nginx:1.17

RUN apt-get update && apt-get install -y gettext-base
COPY ./nginx.conf /etc/nginx/nginx-template.conf
COPY ./custom-index /custom-index/

CMD [ \
  "bash", "-c", \
  "envsubst < /etc/nginx/nginx-template.conf > /etc/nginx/nginx.conf && nginx -g 'daemon off;'" \
]
