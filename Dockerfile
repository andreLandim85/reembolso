FROM eclipse-temurin:21-jre
WORKDIR /work
COPY target/*-runner.jar /work/application.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/work/application.jar"]
