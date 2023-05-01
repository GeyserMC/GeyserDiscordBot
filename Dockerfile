FROM eclipse-temurin:17-alpine

RUN apk update \
	&& apk add tesseract-ocr curl \
	&& adduser -h /opt/app -H -D app

RUN mkdir -p /opt/app && \
    chown app:app /opt/app

USER app:app

WORKDIR /opt/app

COPY target/GeyserBot.jar GeyserBot.jar

EXPOSE 8000

HEALTHCHECK --interval=30s --timeout=30s --start-period=10s \
    --retries=3 CMD [ "sh", "-c", "echo -n 'curl localhost:8000... '; \
    (\
        curl -sf localhost:8000 > /dev/null\
    ) && echo OK || (\
        echo Fail && exit 2\
    )"]

CMD ["java", "-jar", "GeyserBot.jar"]
