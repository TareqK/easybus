![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kisoft/easybus/badge.png)
# EasyBus
Easy bus is a simple, in-memory event bus for java designed for simplicity and 
java practices in mind

* Annotation-Based 
* Sync and Async
* Auto-Registration of Events
* Compile-Time checking for events & handlers

# Useage
## Installation
To use the easybus, add the following into your pom.xml
```xml
<dependency>
  <groupId>me.kisoft</groupId>
  <artifactId>easybs</artifactId>
  <version>${LATEST_VERSION}</version>
</dependency>

```
## Defining Events and Handlers
Afterwards, you will need to define your events and handlers using the ```@Handle```
and ```@Event``` annotations

### Defining Events
```java
@Event
public class MyEvent{
  // your code here
}

```

### Defining Handlers
```java
@Handle(event=MyEvent.class)
public class MyEventHandler{
   public void handle(MyEvent event){
     // your code here
   }
}
```


Note that the ```handle(MyEvent event)``` method is mandatory, and it is the method that will be called by the event bus. The Method **MUST** be called ```handle``` and have the same type as your target event.
### Adding Events and Handlers
You must also create a new event bus and specify the packages or classloaders your 
events are in 

```java
EasyBus bus = new EasyBus();
bus.search("my.package.name")
.search("my.second.package");
```


You will probably need to wrap the event bus as a singleton or maintain some reference to it, but that is just you coding.

### Posting Events
```java
bus.post(new MyEvent());
```

### Async Handlers
Async Handlers are defined in the same way as handlers, except that they run 
in a seperate thread from the current thread
```java
@Handle(event=MyEvent.class,async=true)
public class MyEventAsyncHandler{
   public void handle(MyEvent event){
     // your code here
   }
}
```
#### Increasing Thread Pool Size

By default, the thread pool size is 15 threads to run the events on. You can 
increase or decrease this by using the ```new EasyBus(int numberOfThreads)``` constructor, which will specify the size of the pool


