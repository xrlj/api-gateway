#FROM xinxiamu/alpine-java:11
FROM lpicanco/java11-alpine:latest
VOLUME ["/tmp", "/logs"]
ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app
ENTRYPOINT ["java","-cp","app:app/lib/*","com.xrlj.apigateway.ApiGatewayApplication"]
