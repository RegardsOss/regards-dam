# REGARDS data management (DAM)

This is the DATA MANAGEMENT microservice repository.

For now, this version has not yet been tested under operational conditions and is considered as **beta**.

## Build requirement

Build relies on :
* Maven 3+
* OpenJDK 8

Dependency : 
* REGARDS Bill Of Materials
* REGARDS microservice framework
* REGARDS administration

## Build

```shell
mvn clean install -Dmaven.test.skip=true
```