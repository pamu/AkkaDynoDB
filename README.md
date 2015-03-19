AkkaDynoDB (Reactive Storage Service)
==========================================================

Dynamo like distributed database built using Akka Cluster

**Introduction**

AkkaDynoDB is a _reactive_ _storage_ _service_ inspired from the Amazon dynamo distributed database
which is Highly available, scalable and resilient database [All Things distributed Paper](http://www.allthingsdistributed.com/files/amazon-dynamo-sosp2007.pdf).
With changing requirements of enterprise applications the way we build applications has been changing. With increasing number of
devices that connect to the internet the traffic per server has drastically changed over years and its going to increase much more in the
near future with the emergence of Internet of things(IOT). In order to cater to the changing requirements which cannot be handled by traditional architectures
many enterprises started adopting distributed architectures to spread load over large number of machines and handle user requests. These applications should also
handle data effectively and offer reliability to its users. As the number of users increasing the applications must also scalable well 
according to the need. Not only scalability and reliability users also expect 100% availability that means 0 down times even in case of 
 database maintenance and data migration. In order to offer these essential features enterprises have been adopting certain best practices and techniques.
 _Reactive_ _design_ _principles_ are the basically convey these best practices and how systems should be built from ground up to be scalable, 
 available and be resilient.
 