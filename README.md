# PeopleFlow demo

 
## Open Api specificatin

Please check http://localhost:8080/swagger-ui

## Build and run

```bash
alias dcp="docker-compose"
alias d="docker"
mvn clean install && dcp build && dcp up -d --force-recreate && d logs -f peopleflow
```
