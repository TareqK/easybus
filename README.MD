![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kisoft/easybus-core/badge.png)

# EasyBus

Easy bus is a simple, strictly typed event bus for java designed for simplicity 
and minimalism.

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

Afterwards, you will need to define your events and handlers. Events are simply
POJOs(preferably Beans) and handlers are classes that implement ``Handler<T>``. A Handler
may handle multiple event types by implementing ``Handler<T>`` multiple times with different
arguments.

### Defining Events

```java
public class MyEvent{
  // members and code here
}
```

### Defining Handlers

```java
public class MyEventHandler implements Handler<MyEvent>{
   
   @Override
   public void handle(MyEvent event){
     // your code here
   }
}
```

### Adding Handlers

You must also create a new event bus and specify the packages or classloaders your
events are in

```java
EasyBus bus = new EasyBus();
bus.search("my.package.name")
.search("my.second.package");
```

you can also directly pass a ``Handler`` instance to ``EasyBus`` if you want to avoid
all reflection - this is highly preferable for visibility, buy its up to preference 
and use case and code design.

You will also probably need to wrap the event bus as a singleton or maintain some reference to it, but that is just you coding.

### Posting Events

```java
bus.post(new MyEvent());
```

### Handle Async

The default in-memory event bus(which is also used as a utility internal bus for other implementations)
can either handle events in the current thread or delegate it to a thread pool. To mark an event 
as delegated to a thread pool, use the ``@HandleAsync`` annotation on the event class


```java
@HandleAsync
public class MyAsyncEvent{

}
```

Note that any errors that happen when handling this event are ignored, and as such, its 
highly advised to only use this sparingly.

## Implementing a Backing Bus

To implement a backing bus, all you need to do is implement the `me.kisoft.easybus.BackingBus` Interface, and when
creating a new EasyBus, you need to use the `new EasyBus(BackingBus bus)` constructor to change the backing bus, or
create your own factory
