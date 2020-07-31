## Description of the repository

This repository contains an implementation of [swagger ModelConverter](https://static.javadoc.io/io.swagger/swagger-core/1.5.10/io/swagger/converter/ModelConverter.html), used to generate OpenAPI documentation.

This implementation manages Spring reactor classes:
- [Flux](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Flux.html) are converted to arrays into OpenAPI documentation.
- [Mono](https://projectreactor.io/docs/core/release/api/reactor/core/publisher/Mono.html) are converted to base JSON objects into OpenAPI documentation.

## Useful links for swagger

- [Swagger site](https://swagger.io/)
- [Swagger Maven plugin](https://github.com/kongchen/swagger-maven-plugin)
- [swagger-ui](https://github.com/swagger-api/swagger-ui) - used to server the generated documentation.

## Maven module

- groupId: `fr.irun`
- artifactId: `rocket-swagger`

This module contains all the classes required to convert Model and Properties to OpenAPI document.

## How to use

In addition with [Swagger Maven plugin](https://github.com/kongchen/swagger-maven-plugin):
- Add swagger Maven plugin to pom.xml
- Define the entry point of this module into the configuration:
```xml
<plugin>
    <groupId>com.github.kongchen</groupId>
    <artifactId>swagger-maven-plugin</artifactId>
    <version>${swagger-maven-plugin.version}</version>
    <executions>
        <execution>
            (...)
            <configuration>
                <apiSources>
                    <apiSource>
                    (...)
                        <modelConverters>
                            fr.irun.openapi.swagger.RocketModelConverter
                        </modelConverters>
                        <outputFormats>json</outputFormats>
                    (...)
                    </apiSource>
                    </apiSources>
            </configuration>
        </execution>
    </executions>
    <dependencies>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>${commons-lang3.version}</version>
        </dependency>
        <dependency>
            <groupId>fr.irun</groupId>
            <artifactId>rocket-swagger</artifactId>
            <version>${rocket-swagger.version}</version>
        </dependency>
    </dependencies>
</plugin>
```

## Serve generated file using swagger-ui

After having generated the JSON file, this file can be serve using `swagger-ui` docker.

### Prerequisites

- Docker shall be installed

### Create the folder containing OpenAPI file

```
sudo mkdir -p /mnt/swagger
sudo chown $USER:$USER /mnt/swagger
mkdir -p /mnt/swagger/api
```

### Pull docker image<id>swagger</id>
```
docker pull swaggerapi/swagger-ui
```

### Copy the generated JSON file

```
cp <path-to-generated-json>/swagger.json /mnt/swagger/api/
```

### Launch the container

```
docker run -p 80:8080 -e SWAGGER_JSON=/api/swagger.json -v /mnt/swagger/api:/api swaggerapi/swagger-ui
```

### Consult documentation
- The documentation is now available on http://localhost.


