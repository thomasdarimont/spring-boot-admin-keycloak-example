# Protecting Spring Boot Admin & Actuator Endpoints with Keycloak

This example consists of a [spring-boot-admin](https://github.com/codecentric/spring-boot-admin) server application `admin-service` which monitors
another application called `todo-service` build with Spring Boot.  
The `admin-service` exposes the Spring Boot Admin UI via the `/admin` endpoint which is protected by the Keycloak adapter.  
The actuator endpoints of the `todo-service` are also protected with Keycloak and accessed via a `service-account` configured for the `admin-service` Keycloak client.

This example is currently build with:
- Spring Boot 2.0.3
- Spring Boot Admin 2.0.1
- Keycloak 4.1.0 

Note that an older version of this example is available in the 1.5.x branch, which uses:
- Spring Boot 1.5.13
- Spring Boot Admin 1.5.7
- Keycloak 3.4.3.Final.

# Setup Keycloak

Import `bootadmin` demo realm by executing the following command in the `KEYCLOAK_HOME` directory.   
```
 bin/standalone.sh -Dkeycloak.migration.action=import \
  -Dkeycloak.migration.provider=singleFile \
  -Dkeycloak.migration.file=/path/to/bootadmin-realm.json \
  -Dkeycloak.migration.strategy=OVERWRITE_EXISTING
```

After that Keycloak should be running with the `bootadmin` realm loaded in Keycloaks in-memory database.  
You can stop Keycloak with `CTRL+C`. You can start it again by running `bin/standalone.sh`.

# Build the examples

Run `mvn clean package` in the project root.

# Run the examples

## Run the todo-service
The simple `todo-service` can be reached via http://localhost:30002  
To start the service just run `java -jar todo-service/target/*.jar`

## Run the admin-service
The `admin-service` can be reached via http://localhost:30001/admin  
To start the service just run `java -jar admin-service/target/*.jar`
