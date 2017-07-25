# stubby-maven-plugin
Maven plugin to start and stop stubby4j as part of the maven build lifecycle

This can be useful when you are spinning up an application for integration testing and it needs to connect to another HTTP service.
Combine with a port finder plugin for concurrent usage on build servers

You can find configuration info for the stubs.yaml file itself on the [Stubby4J project page](https://github.com/azagniotov/stubby4j)

## Configuration

Default values for configuration shown

```xml
<build>
<plugins>
  <plugin>
    <groupId>com.testingsyndicate</groupId>
    <artifactId>stubby-maven-plugin</artifactId>
    <version>1.0.0</version>
    <executions>
      <execution>
        <goals>
          <goal>start</goal>
          <goal>stop</goal>
        </goals>
        <configuration>
          <configFile>target/stubs.yaml</configFile>
          <httpPort>8882</httpPort>
          <httpsPort>7443</httpsPort>
          <adminPort>8889</adminPort>
        </configuration>
      </execution>
    </executions>
  </plugin>
</plugins>
</build>
```

You can also quickly start stubby to manually validate your stubs file

`mvn stubby:run -DconfigFile=path/to/stubs.yaml`
