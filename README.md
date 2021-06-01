# PeopleFlow demo

## Open Api specificatin

Please check http://localhost:8080/swagger-ui

## How to build and run

```bash
mvn clean install && docker-compose build && docker-compose up -d --force-recreate && docker logs -f peopleflow
```

## Rest endpoints

This application exposes one main employee controller. This controller has 3 types of operations:

| Operation  | Link |
| ------------- | ------------- |
| Create employee  | [PUT](http://localhost:8080/swagger-ui/#/employee-controller/createEmployeeUsingPUT)  |
| Update employee status  | [POST](http://localhost:8080/swagger-ui/#/employee-controller/sendEmployeeEventUsingPOST)   |
| Get employee info | [GET](http://localhost:8080/swagger-ui/#/employee-controller/getEmployeesInfoUsingGET) |


