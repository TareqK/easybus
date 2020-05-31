# EasyBus
Easy bus is a simple, in-memory event bus for java designed for simplicity and 
java practices in mind

* Annotation-Based 
* Sync and Async
* Auto-Registration of Events
* Compile-Time checking for events & handlers

# Useage
To use the easybus, add the following into your pom.xml
```xml
<dependency>
  <groupId>me.kisoft</groupId>
  <artifactId>easybs</artifactId>
  <version>${LATEST_VERSION}</version>
</dependency>

```

Afterwards, you will need to define your events and handlers using the ```@Handle```
and ```@Event``` annotations

```java
@Event
public class MyEvent{
  // your code here
}

```


```java
@Handle(event=MyEvent.class)
public class MyEventHandler{
   public void handle(MyEvent event){
     // your code here
   }
}
```

Note that the ```handle(MyEvent event)``` method is mandatory, and it is the method that will be called by the event bus. The Method **MUST** be called ```handle``` and have the same type as your target event.

Event Handling based on inheritance is not yet possible, but will be looked into in 
the future.

