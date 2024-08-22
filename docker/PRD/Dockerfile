FROM openjdk:8-jdk-alpine
RUN mkdir /app
COPY zf-compressor-services.jar /app
ENV TZ=America/Bogota
RUN apk update && apk add bash
CMD ["java", "-server", "-Xms500m", "-Xmx500m", "-XX:+UseG1GC", "-XX:PermSize=256m", "-jar", "/app/zf-compressor-services.jar"]
