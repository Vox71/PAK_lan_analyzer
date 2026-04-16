FROM alpine:3.19

RUN apk add --no-cache arp-scan bash

COPY scan.sh /scan.sh
RUN chmod +x /scan.sh

CMD ["/scan.sh"]