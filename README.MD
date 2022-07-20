![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kisoft/easybus-core/badge.png)

# EasyBus

Easy bus is a simple, event bus for java designed for simplicity and
java practices in mind

-   Annotation-Based
-   Sync and Async
-   Auto-Registration of Events
-   Compile-Time checking for events & handlers

# Usage

## Installation

To use the easybus, add the following into your pom.xml

```xml
<dependency>
  <groupId>me.kisoft</groupId>
  <artifactId>easybus-core</artifactId>
  <version>${LATEST_VERSION}</version>
</dependency>

```

If you want to use a specific backing implementation of easybus, you can also import
that specific implementation which will also import easybus

## Defining Events and Handlers

Afterwards, you will need to define your events and handlers using the `@Handle`
and `@Event` annotations

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

Note that the `handle(MyEvent event)` method is mandatory, and it is the method that will be called by the event bus. The Method **MUST** be called `handle` and have the same type as your target event.

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
in a seperate thread from the current thread. This is more of a hint to the backing bus;
its not mandatory to honor async execution.

```java
@Handle(event=MyEvent.class,async=true)
public class MyEventAsyncHandler{
   public void handle(MyEvent event){
     // your code here
   }
}
```

## Implementing a Backing Bus

To implement a backing bus, all you need to do is implement the `me.kisoft.easybus.Bus` Interface, and when
creating a new EasyBus, you need to use the `new EasyBus(Bus bus)` constructor to change the backing bus, or
create your own factory
