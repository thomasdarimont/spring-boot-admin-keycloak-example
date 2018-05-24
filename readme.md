# Protecting Spring Boot Admin & Actuator Endpoints with Keycloak

This example consists of a [spring-boot-admin](https://github.com/codecentric/spring-boot-admin) server application `admin-service` which monitors
one other service instance `todo-service`. The `admin-service` exposes the spring-boot admin
UI via the `/admin` endpoint which is protected with Keycloak.
The client actuator endpoints of the `todo-service` are also protected with Keycloak and accessed
via a `service-account` configured for the `admin-service` client.

# Setup Keycloak

Import `bootadmin` demo realm by executing the following command in the `KEYCLOAK_HOME` directory. 
```
 bin/standalone.sh -Dkeycloak.migration.action=import \
  -Dkeycloak.migration.provider=singleFile \
  -Dkeycloak.migration.file=/path/to/bootadmin-realm.json \
  -Dkeycloak.migration.strategy=OVERWRITE_EXISTING
```

After that Keycloak should be running with the realm loaded in Keycloaks In-Memory Database.  
You can stop Keycloak with `CTRL+C`. You can start it again by running `bin/standalone.sh`.

# Build the examples

Run `mvn clean package` in the project root.

# Run the examples

## Run the todo-service
The simple todo-service can be reached via http://localhost:30002  
To start the service just run `java -jar todo-service/target/*.jar`

## Run the admin-service
The admin-service can be reached via http://localhost:30001/admin  
To start the service just run `java -jar admin-service/target/*.jar`
