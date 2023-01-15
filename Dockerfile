FROM eclipse-temurin:17-alpine

RUN apk update \
	&& apk add tesseract-ocr \
	&& adduser -h /opt/app -H -D app

RUN mkdir -p /opt/app && \
    chown app:app /opt/app

USER app:app

WORKDIR /opt/app

CMD ["java", "-jar", "GeyserBot.jar"]