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

## EXTRA. 

### What about the silver bullet reviewing this solution?

This solution consists of 2 parts: first - we use kafka to publish events, second - we use spring state machine to handle state changes.

The idea to use kafka as an 'integration bus' is a fact, that we could effectively separate producers from consumers.

For example, when creating 'employee' we can have different systems that publish 'create' events to common topic. We 
can integrate systems that doing batch creates from csv data, and systems that requires manual interactions, like 
web based UI forms. With one same topic, different system can publish events not disturbing each other.

From the point of view of event consumers - we can also configure different types of services that interested in employee base events - like a service that use spring state machines and in close-to-real time can determine 
employee state based on employee email. What does means 'close-to-real' time here? There could be a time gap between 
the fact when event got published to topic and when event got read from that topic and being processed. If it is not 
expected to have some 
transactions on changing employee states, it will be ok to have kafka topic for that.

Having effectively separated readers and writers, it is possibly easily scale such system, use event-based frameworks, like spring-webflux.

Having all that in mind, being familiar with event-based processing concepts, understanding distributed systems 
architecture - can be silver bullet that helps to review different solutions and make sure that software is of high 
quality, it is resilient and can be expanded later.


### What is the production readiness criteria for that solution?

Production readiness is a state when software can solve problem, it was designed to solve to. It is 
solves that problem with acceptable processing time and desired load.

This software is able to determining state of employee per email, 
ignoring employee
other info. Also plain states graph is too simple to advocate usage of 
state-machines, so this is a case where plain if/else still could benefit then spring state-machines.

State machines rely on fact that events should come in some predefined order. Kafka keeps the order of incoming 
messages only per partition. So that means when we do not define proper partitioning key - we can not guarantee 
messages will be read in same order that they were put into topic.
In this implementation we have only one partition in kafka, so it is ok not to use partitions keys, but for topics 
with multiple partitions that could be an issue.

Main reason of that project was to show how kafka and state-machines could be used together to handle employee 
states switching. This is the main problem this example is going to solve and it's main purpose is demonstration of
possibility to use these technologies together.

## EXTRA

### How to collect statistic about employees?

To collect some statistic, we need to define source of the raw statistic data. We have kafka topic which is used 
as 'enterprise bus' to publish employees events. So we could always configure kafka listener with different group to 
read this topic. Since 
kafka allow multiple parallel readers, we could let the services decide how to handle their own event 
processing and data collection.

Other source of employee information is in-memory store of state machine contexts, which can also be queried for 
statistics.

From my point of view integration through kafka (with strictly defined data contract for messages) can be preferable 
integration pattern.

If it will be necessary we could configure one more kafka producer and using state machine actions publish events 
when some state machines reach desired states. For example if we interested in statistic of customers with 
'APPROVED' state, we could publish event every time customer got 'APPROVED'.

## Links

[Spring state machine](https://docs.spring.io/spring-statemachine/docs/3.0.0/reference/)

[Spring web flux](https://docs.spring.io/spring-statemachine/docs/current/reference/)






