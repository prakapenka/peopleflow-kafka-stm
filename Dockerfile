FROM eclipse-temurin:17

RUN addgroup spring && useradd -M spring -g spring

USER spring:spring

COPY /target/peopleflow.jar /opt/

ENTRYPOINT ["java", "-Duser.timezone=CET", "-jar","/opt/peopleflow.jar", "--spring.config.location=${CONFIG_LOCATION}"]
