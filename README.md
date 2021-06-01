# PeopleFlow demo

Simple demo of web application that uses spring-boot, kafka and state-machine to add some employees and changing 
employees states.

This example requires `java11` `maven` `docker` and `docker compose` to be installed. See how to build and run 
section below.

## Open Api specificatin

Please check http://localhost:8080/swagger-ui

## How to build and run

All command in one line:
```bash
mvn clean install && docker-compose build && docker-compose up -d --force-recreate && docker logs -f peopleflow
```

Explanation:

| Command | Purpose |
| --- | --- |
| `mvn clean install` | compile and prepare server package  |
| `docker-compose build` | builds required server docker image |
| `docker-compose up -d --force-recreate` | runs kafka, zookeeper and server container |
| `docker logs -f peopleflow` | after start of server container, monitor log container log output |


## Rest endpoints

This application exposes one main employee controller. This controller has 3 types of operations:

| Operation  | Link |
| ------------- | ------------- |
| Create employee  | [PUT](http://localhost:8080/swagger-ui/#/employee-controller/createEmployeeUsingPUT)  |
| Update employee status  | [POST](http://localhost:8080/swagger-ui/#/employee-controller/sendEmployeeEventUsingPOST)   |
| Get employee info | [GET](http://localhost:8080/swagger-ui/#/employee-controller/getEmployeesInfoUsingGET) |

### How it is works all together?

1. Call 'Get employee info' endpoint to check current employee states
1. Create employee with email "john.smith@example.com" using 'Create employee' endpoint
1. Update employee state using 'Update employee status' endpoint

## State machine states

_NOT_EXISTED_ &#8594; **ADDED** &#8594; **IN_CHECK** &#8594; **APPROVED** &#8594; **ACTIVE** 

| State | Description |
| ----- | ----------- |
| _NOT_EXISTED_ | Every state machine is initialized with this state. Events for improper state also will lead to such state. For example, attempt to set newly created state machine directly to **APPROVED** state skipping **IN_CHECK** and **ADDED** state will lead to state machine remains with _NOT_EXISTED_ state |
| **ADDED** | State machine switching to this state in case of 'CREATE' event. To simulate CREATE employee event please use this endpoint: [PUT](http://localhost:8080/swagger-ui/#/employee-controller/createEmployeeUsingPUT)
| **IN_CHECK** | In check state will be reached if state machine already in previous **ADDED** state and for employee event 'CHECK' was fired. To simulate this event please use 	[POST](http://localhost:8080/swagger-ui/#/employee-controller/sendEmployeeEventUsingPOST) enpoint.
| **APPROVED** | This state can be reached after **IN_CHECK** state. Use [POST](http://localhost:8080/swagger-ui/#/employee-controller/sendEmployeeEventUsingPOST) endpoint. |
| **ACTIVE** | Can be reached after previous **APPROVED** state, please use  [POST](http://localhost:8080/swagger-ui/#/employee-controller/sendEmployeeEventUsingPOST) endpoint. |

## EXTRA. How to collect statistic about employees?

To collect some statistic, we need to define source of the raw statistic data. We have kafka topic which is used 
as 'enterprise bus' to publish employees events. So we could always configure kafka listener with different group to 
read this topic. Since 
kafka allow multiple parallel readers, we could let the services decide how to handle their own event 
processing and data collection.

Other source of employee information is in-memory store of state machine contexts, which can also be queried for 
statistics.

From my point of vew integration through kafka (with strictly defined data contract for messages) can be preferable 
integration pattern.

If it would be neccessary we could configure one more kafka producer and using state machine actions publish events 
when some state machines reaches desired states. For example if we interested in statistic of customers with 
'APPROVED' state, we could publish event every time customer got 'APPROVED'.

## Links

[Spring state machine](https://docs.spring.io/spring-statemachine/docs/3.0.0/reference/)

[Spring web flux](https://docs.spring.io/spring-statemachine/docs/current/reference/)






