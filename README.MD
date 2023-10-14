# EasyBus
![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.kisoft/easybus-core/badge.png)

Easy bus is a simple, strictly typed event bus for java designed for simplicity 
and minimalism.

<hr/>

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

## Defining Events and Listeners

Afterwards, you will need to define your events and listeners. Events are simply
POJOs(preferably Beans) and listeners are classes that implement ``Listener<T>``. A Listener
may only listen for a single event(and its subclasses) - this is due to type erasure in 
java for generics that disallows multi inheritance.

### Defining Events

```java
public class MyEvent{
  // members and code here
}
```

### Defining Listeners

```java
public class MyEventListener implements Listener<MyEvent>{
   
   @Override
   public void on(MyEvent event){
     // your code here
   }
}
```

### Adding Listeners

```java
EasyBus bus = new EasyBus();
bus.register(new MyEventListener());
```

You will also probably need to wrap the event bus as a singleton or
maintain some reference to it, but thats up to the programmer.

### Activation/DI

If you want to use DI in your event listeners, you can do that by passing a custom 
activator to easybus on creation.
The activator is a functional interface that takes a class that extends ``Listener``
and returns a new instance.

In this example, we assume you are using ``Guice`` as your DI provider

```java
EasyBus bus = new EasyBus(injector::getInstance);
bus.register(MyEventListenerUsingDI.class);
```

by default, the built in activator attempts to  call a no-args constructor.

### Posting Events

```java
bus.post(new MyEvent());
```

### Handle Async

The default in-memory event bus(which is also used as a utility internal bus for other implementations)
can either process events in the current thread or delegate it to a thread pool. To mark an event 
as delegated to a thread pool, use the ``@ProcessAsync`` annotation on the event class


```java
@ProcessAsync
public class MyAsyncEvent{

}
```

Note that any errors that happen when processing this event are ignored, and as such, its 
highly advised to only use this sparingly.

## Implementing a Backing Bus

To implement a backing bus, all you need to do is implement the ``me.kisoft.easybus.BackingBus`` Interface, and when
creating a new EasyBus, you need to use the ``new EasyBus(BackingBus bus)`` constructor to change the backing bus, or
create your own factory
