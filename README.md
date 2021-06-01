# PeopleFlow demo

## Open Api specificatin

Please check http://localhost:8080/swagger-ui

## Build and run

```bash
mvn clean install && docker-compose build && docker-compose up -d --force-recreate && docker logs -f peopleflow
```
