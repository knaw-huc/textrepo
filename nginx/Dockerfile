FROM nginx:1.25.3

RUN apt-get update && apt-get install -y gettext-base
COPY ./nginx.conf /etc/nginx/nginx-template.conf
COPY ./scripts/start.sh /start.sh
COPY ./custom-index /custom-index/

CMD ["./start.sh"]
