AkkaDynoDB (Reactive Storage Service)
==========================================================

Dynamo like distributed database built using Akka Cluster

## **Introduction**

_**AkkaDynoDB**_ is a _reactive_ _storage_ _service_ inspired from the Amazon dynamo distributed database
which is Highly available, scalable and resilient database [All Things distributed Paper](http://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf).
With changing requirements of enterprise applications the way we build applications has been changing. With increasing number of
devices that connect to the internet the traffic per server has drastically changed over years and its going to increase much more in the
near future with the emergence of Internet of things(IOT). In order to cater to the changing requirements which cannot be handled by traditional architectures
many enterprises started adopting distributed architectures to spread load over large number of machines and handle user requests. These applications should also
handle data effectively and offer reliability to its users. As the number of users increasing the applications must also scalable well 
according to the need. Not only scalability and reliability users also expect 100% availability that means 0 down times even in case of 
database maintenance and data migration. In order to offer these essential features enterprises have been adopting certain best practices and techniques from 
different researches. _**Reactive design principles**_ are the basically convey these best practices and how systems should be built from ground up to be scalable,
available and be resilient.
 
_**Reactive Systems**_ has these four traits

1. Responsive     (Available in a responding state)

2. Resilient      (Fault tolerant)

3. Elastic        (Scalable)

4. Message Driven (Communicate by sending messages i.e core to distribution)

Please read [Reactive Manifesto](http://www.reactivemanifesto.org/) to know about what is to be reactive.

_**Akka**_ as mentioned here [akka.io](http://akka.io) is a toolkit and runtime for building 

1. Highly Concurrent

2. Distributed

3. Resilient

4. Message-Driven  applications on JVM (Java Virtual Machine)

Akka adheres to the reactive principles and offers abstractions to deal with concurrency, distribution and fault tolerance.

1. For concurrency Akka offers Scala Futures and Agents.    (Helps Scale up)

2. For Distribution and Remoting it offers Actors.          (Helps Scale Out)

3. For Fault tolerance Actor Supervision.                   (Deal with Failure)

Note: The above mentioned is not exhaustive list of what akka offers. Please visit (Akka.io)[http://akka.io] for more.

More about actors (Actor Model)[http://arxiv.org/pdf/1008.1459.pdf], (Actor Model)[http://publications.csail.mit.edu/lcs/pubs/pdf/MIT-LCS-TR-194.pdf].






 