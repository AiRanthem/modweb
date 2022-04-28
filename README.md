# ModWeb
ModWeb is a 'Web Framework' but runs on Modbus. It helps you to define a service in
Spring Boot and request the services you defined. ModWeb even simplifies the way you
call a raw Modbus request very easily.

## Install

Just import it in your spring boot project.

```xml
<dependency>
    <groupId>cn.airanthem</groupId>
    <artifactId>modweb-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

> Because `ModWeb` is a new project and is still developing, when you read this document it stands a good chance
> that I haven't upload ModWeb to any public maven repository yet !-_-
> 
> Maybe you can `clone this repository` and run `mvn install` to install locally :)

## Configuration

Add the properties below to your Spring Boot config file:

```yaml
modweb:
  port: 50200    # the port that modbus will listen at
  timeout: 10000 # set timeout time (milliseconds) when requesting any modbus slave
```

## Define a Service

```java
import cn.airanthem.modweb.annotation.ModWebService;
import cn.airanthem.modweb.iface.ModWebHandler;

import javax.annotation.Resource;

/**
 * a ModWeb service is registered when a class:
 *
 * 1. a `@ModWebService` annotation is put on it
 * 2. this class implements interface `ModWebHandler`
 */
@ModWebService(name = "service name")
public class MyService implements ModWebHandler {
    /**
     * for that ModWebService is a Spring Boot Component,
     * you can inject other Beans.
     */
    @Resource
    OtherBean someOtherBean;

    @Override
    public byte[] handle(byte[] payload) {
        byte[] result = someOtherBean.doSomething(payload);
        /**
         * the return value of method `handle` will be transferred
         * to the client
         */
        return result;
    }
}
```

## Make Requests

A `ModWebClient` Bean is provided to make any Modbus request.

### Preparation

You must initialize the ModWebClient at any place you like before using it.

Take the code below as an example

```java
import cn.airanthem.modweb.client.ModWebClient;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Component
public class SomeComponent {
    @Resource
    ModWebClient modWebClient;

    @PostConstruct
    public void init() {
        /**
         * Params: id, ip, port
         * the first param `id` is very important. you should use the id to
         * specify which peer(s) should be called. just use some way to save them.
         */
        modWebClient.putPeer(1, "192.168.1.111", 502);
        modWebClient.putPeer(2, "192.168.1.112", 502);
        modWebClient.putPeer(3, "192.168.1.113", 502);
    }
}
```

### Request a ModWeb Service

```java
import cn.airanthem.modweb.client.ModWebClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class SomeComponent {
    @Resource
    ModWebClient modWebClient;

    /**
     * in this example, all peers registered before (1/2/3) will be requested
     * with same service name and payload data.
     *
     * the resultMap maps the ID of each peer and the data it returns 
     */
    public void requestAllPeers() {
        Map<Integer, ModWebClient.Result> resultMap = modWebClient
                .all().requestService("service name", new byte[]{1, 2, 3, 4, 5});
    }

    /**
     * just use `selected` instead of `all` to specify which peer(s) you want to request
     */
    public void requestSelectedPeers() {
        Map<Integer, ModWebClient.Result> resultMap = modWebClient
                .selected(1, 2).requestService("service name", new byte[]{1, 2, 3, 4, 5});
    }
}
```

## Make an Original Modbus Request
> **CAUTION** `ModWeb` will use Modbus function `ReadWriteMultipleRegisters`, pleasu do avoid this function
> at the port `ModWeb` listens. I suggest that you should use a specific port to run ModWeb Services without
> any other usage.

```java
import cn.airanthem.modweb.client.ModWebClient;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
public class SomeComponent {
    @Resource
    ModWebClient modWebClient;

    /**
     * just use the raw Modbus function name instead of `requestService`.
     * please usd `CTRL/CMD + P` in IDEA to view the param list 'cause I'm lazy :P
     */
    public void requestAllPeers() {
        Map<Integer, ModWebClient.Result> resultMap = modWebClient
                .all().readInputRegister(0, 10, 0);
    }
}
```