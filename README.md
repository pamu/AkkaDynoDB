AkkaDynoDB(Reactive Storage Service)
==========

Dynamo like distributed database built using Akka Cluster

Cluster Usage
For introduction to the Akka Cluster concepts please see Cluster Specification.

Preparing Your Project for Clustering
The Akka cluster is a separate jar file. Make sure that you have the following dependency in your project:

"com.typesafe.akka" %% "akka-cluster" % "2.4-SNAPSHOT"
A Simple Cluster Example
The following configuration enables the Cluster extension to be used. It joins the cluster and an actor subscribes to cluster membership events and logs them.

The application.conf configuration looks like this:

akka {
  actor {
    provider = "akka.cluster.ClusterActorRefProvider"
  }
  remote {
    log-remote-lifecycle-events = off
    netty.tcp {
      hostname = "127.0.0.1"
      port = 0
    }
  }
 
  cluster {
    seed-nodes = [
      "akka.tcp://ClusterSystem@127.0.0.1:2551",
      "akka.tcp://ClusterSystem@127.0.0.1:2552"]
 
    auto-down-unreachable-after = 10s
  }
}
 
# Disable legacy metrics in akka-cluster.
akka.cluster.metrics.enabled=off
 
# Enable metrics extension in akka-cluster-metrics.
akka.extensions=["akka.cluster.metrics.ClusterMetricsExtension"]
 
# Sigar native library extract location during tests.
# Note: use per-jvm-instance folder when running multiple jvm on one host. 
akka.cluster.metrics.native-library-extract-folder=${user.dir}/target/native
To enable cluster capabilities in your Akka project you should, at a minimum, add the Remoting settings, but with akka.cluster.ClusterActorRefProvider. The akka.cluster.seed-nodes should normally also be added to your application.conf file.

The seed nodes are configured contact points for initial, automatic, join of the cluster.

Note that if you are going to start the nodes on different machines you need to specify the ip-addresses or host names of the machines in application.conf instead of 127.0.0.1

An actor that uses the cluster extension may look like this:

package sample.cluster.simple
 
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.actor.ActorLogging
import akka.actor.Actor
 
class SimpleClusterListener extends Actor with ActorLogging {
 
  val cluster = Cluster(context.system)
 
  // subscribe to cluster changes, re-subscribe when restart 
  override def preStart(): Unit = {
    //#subscribe
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
    //#subscribe
  }
  override def postStop(): Unit = cluster.unsubscribe(self)
 
  def receive = {
    case MemberUp(member) =>
      log.info("Member is Up: {}", member.address)
    case UnreachableMember(member) =>
      log.info("Member detected as unreachable: {}", member)
    case MemberRemoved(member, previousStatus) =>
      log.info("Member is Removed: {} after {}",
        member.address, previousStatus)
    case _: MemberEvent => // ignore
  }
}
The actor registers itself as subscriber of certain cluster events. It receives events corresponding to the current state of the cluster when the subscription starts and then it receives events for changes that happen in the cluster.

The easiest way to run this example yourself is to download Typesafe Activator and open the tutorial named Akka Cluster Samples with Scala. It contains instructions of how to run the SimpleClusterApp.

Joining to Seed Nodes
You may decide if joining to the cluster should be done manually or automatically to configured initial contact points, so-called seed nodes. When a new node is started it sends a message to all seed nodes and then sends join command to the one that answers first. If no one of the seed nodes replied (might not be started yet) it retries this procedure until successful or shutdown.

You define the seed nodes in the Configuration file (application.conf):

akka.cluster.seed-nodes = [
  "akka.tcp://ClusterSystem@host1:2552",
  "akka.tcp://ClusterSystem@host2:2552"]
This can also be defined as Java system properties when starting the JVM using the following syntax:

-Dakka.cluster.seed-nodes.0=akka.tcp://ClusterSystem@host1:2552
-Dakka.cluster.seed-nodes.1=akka.tcp://ClusterSystem@host2:2552
The seed nodes can be started in any order and it is not necessary to have all seed nodes running, but the node configured as the first element in the seed-nodes configuration list must be started when initially starting a cluster, otherwise the other seed-nodes will not become initialized and no other node can join the cluster. The reason for the special first seed node is to avoid forming separated islands when starting from an empty cluster. It is quickest to start all configured seed nodes at the same time (order doesn't matter), otherwise it can take up to the configured seed-node-timeout until the nodes can join.

Once more than two seed nodes have been started it is no problem to shut down the first seed node. If the first seed node is restarted, it will first try to join the other seed nodes in the existing cluster.

If you don't configure seed nodes you need to join the cluster programmatically or manually.

Manual joining can be performed by using ref:cluster_jmx_scala or Command Line Management. Joining programatically can be performed with Cluster(system).join.

You can join to any node in the cluster. It does not have to be configured as a seed node. Note that you can only join to an existing cluster member, which means that for bootstrapping some node must join itself.

You may also use Cluster(system).joinSeedNodes to join programmatically, which is attractive when dynamically discovering other nodes at startup by using some external tool or API. When using joinSeedNodes you should not include the node itself except for the node that is supposed to be the first seed node, and that should be placed first in parameter to joinSeedNodes.

Unsuccessful join attempts are automatically retried after the time period defined in configuration property retry-unsuccessful-join-after. When using seed-nodes this means that a new seed node is picked. When joining manually or programatically this means that the last join request is retried. Retries can be disabled by setting the property to off.

An actor system can only join a cluster once. Additional attempts will be ignored. When it has successfully joined it must be restarted to be able to join another cluster or to join the same cluster again. It can use the same host name and port after the restart, but it must have been removed from the cluster before the join request is accepted.

Automatic vs. Manual Downing
When a member is considered by the failure detector to be unreachable the leader is not allowed to perform its duties, such as changing status of new joining members to 'Up'. The node must first become reachable again, or the status of the unreachable member must be changed to 'Down'. Changing status to 'Down' can be performed automatically or manually. By default it must be done manually, using JMX or Command Line Management.

It can also be performed programatically with Cluster(system).down(address).

You can enable automatic downing with configuration:

akka.cluster.auto-down-unreachable-after = 120s
This means that the cluster leader member will change the unreachable node status to down automatically after the configured time of unreachability.

Be aware of that using auto-down implies that two separate clusters will automatically be formed in case of network partition. That might be desired by some applications but not by others.

Note
If you have auto-down enabled and the failure detector triggers, you can over time end up with a lot of single node clusters if you don't put measures in place to shut down nodes that have become unreachable. This follows from the fact that the unreachable node will likely see the rest of the cluster as unreachable, become its own leader and form its own cluster.

Leaving
There are two ways to remove a member from the cluster.

You can just stop the actor system (or the JVM process). It will be detected as unreachable and removed after the automatic or manual downing as described above.

A more graceful exit can be performed if you tell the cluster that a node shall leave. This can be performed using JMX or Command Line Management. It can also be performed programatically with Cluster(system).leave(address).

Note that this command can be issued to any member in the cluster, not necessarily the one that is leaving. The cluster extension, but not the actor system or JVM, of the leaving member will be shutdown after the leader has changed status of the member to Exiting. Thereafter the member will be removed from the cluster. Normally this is handled automatically, but in case of network failures during this process it might still be necessary to set the node’s status to Down in order to complete the removal.

Subscribe to Cluster Events
You can subscribe to change notifications of the cluster membership by using Cluster(system).subscribe.

cluster.subscribe(self, classOf[MemberEvent], classOf[UnreachableMember])
A snapshot of the full state, akka.cluster.ClusterEvent.CurrentClusterState, is sent to the subscriber as the first message, followed by events for incremental updates.

Note that you may receive an empty CurrentClusterState, containing no members, if you start the subscription before the initial join procedure has completed. This is expected behavior. When the node has been accepted in the cluster you will receive MemberUp for that node, and other nodes.

If you find it inconvenient to handle the CurrentClusterState you can use ClusterEvent.InitialStateAsEvents as parameter to subscribe. That means that instead of receiving CurrentClusterState as the first message you will receive the events corresponding to the current state to mimic what you would have seen if you were listening to the events when they occurred in the past. Note that those initial events only correspond to the current state and it is not the full history of all changes that actually has occurred in the cluster.

cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
  classOf[MemberEvent], classOf[UnreachableMember])
The events to track the life-cycle of members are:

ClusterEvent.MemberUp - A new member has joined the cluster and its status has been changed to Up.
ClusterEvent.MemberExited - A member is leaving the cluster and its status has been changed to Exiting Note that the node might already have been shutdown when this event is published on another node.
ClusterEvent.MemberRemoved - Member completely removed from the cluster.
ClusterEvent.UnreachableMember - A member is considered as unreachable, detected by the failure detector of at least one other node.
ClusterEvent.ReachableMember - A member is considered as reachable again, after having been unreachable. All nodes that previously detected it as unreachable has detected it as reachable again.
There are more types of change events, consult the API documentation of classes that extends akka.cluster.ClusterEvent.ClusterDomainEvent for details about the events.

Instead of subscribing to cluster events it can sometimes be convenient to only get the full membership state with Cluster(system).state. Note that this state is not necessarily in sync with the events published to a cluster subscription.

Worker Dial-in Example
Let's take a look at an example that illustrates how workers, here named backend, can detect and register to new master nodes, here named frontend.

The example application provides a service to transform text. When some text is sent to one of the frontend services, it will be delegated to one of the backend workers, which performs the transformation job, and sends the result back to the original client. New backend nodes, as well as new frontend nodes, can be added or removed to the cluster dynamically.

Messages:

final case class TransformationJob(text: String)
final case class TransformationResult(text: String)
final case class JobFailed(reason: String, job: TransformationJob)
case object BackendRegistration
The backend worker that performs the transformation job:

class TransformationBackend extends Actor {
 
  val cluster = Cluster(context.system)
 
  // subscribe to cluster changes, MemberUp
  // re-subscribe when restart
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)
 
  def receive = {
    case TransformationJob(text) => sender() ! TransformationResult(text.toUpperCase)
    case state: CurrentClusterState =>
      state.members.filter(_.status == MemberStatus.Up) foreach register
    case MemberUp(m) => register(m)
  }
 
  def register(member: Member): Unit =
    if (member.hasRole("frontend"))
      context.actorSelection(RootActorPath(member.address) / "user" / "frontend") !
        BackendRegistration
}
Note that the TransformationBackend actor subscribes to cluster events to detect new, potential, frontend nodes, and send them a registration message so that they know that they can use the backend worker.

The frontend that receives user jobs and delegates to one of the registered backend workers:

class TransformationFrontend extends Actor {
 
  var backends = IndexedSeq.empty[ActorRef]
  var jobCounter = 0
 
  def receive = {
    case job: TransformationJob if backends.isEmpty =>
      sender() ! JobFailed("Service unavailable, try again later", job)
 
    case job: TransformationJob =>
      jobCounter += 1
      backends(jobCounter % backends.size) forward job
 
    case BackendRegistration if !backends.contains(sender()) =>
      context watch sender()
      backends = backends :+ sender()
 
    case Terminated(a) =>
      backends = backends.filterNot(_ == a)
  }
}
Note that the TransformationFrontend actor watch the registered backend to be able to remove it from its list of available backend workers. Death watch uses the cluster failure detector for nodes in the cluster, i.e. it detects network failures and JVM crashes, in addition to graceful termination of watched actor. Death watch generates the Terminated message to the watching actor when the unreachable cluster node has been downed and removed.

The Typesafe Activator tutorial named Akka Cluster Samples with Scala. contains the full source code and instructions of how to run the Worker Dial-in Example.

Node Roles
Not all nodes of a cluster need to perform the same function: there might be one sub-set which runs the web front-end, one which runs the data access layer and one for the number-crunching. Deployment of actors—for example by cluster-aware routers—can take node roles into account to achieve this distribution of responsibilities.

The roles of a node is defined in the configuration property named akka.cluster.roles and it is typically defined in the start script as a system property or environment variable.

The roles of the nodes is part of the membership information in MemberEvent that you can subscribe to.

How To Startup when Cluster Size Reached
A common use case is to start actors after the cluster has been initialized, members have joined, and the cluster has reached a certain size.

With a configuration option you can define required number of members before the leader changes member status of 'Joining' members to 'Up'.

akka.cluster.min-nr-of-members = 3
In a similar way you can define required number of members of a certain role before the leader changes member status of 'Joining' members to 'Up'.

akka.cluster.role {
  frontend.min-nr-of-members = 1
  backend.min-nr-of-members = 2
}
You can start the actors in a registerOnMemberUp callback, which will be invoked when the current member status is changed tp 'Up', i.e. the cluster has at least the defined number of members.

Cluster(system) registerOnMemberUp {
  system.actorOf(Props(classOf[FactorialFrontend], upToN, true),
    name = "factorialFrontend")
}
This callback can be used for other things than starting actors.

Cluster Singleton
For some use cases it is convenient and sometimes also mandatory to ensure that you have exactly one actor of a certain type running somewhere in the cluster.

This can be implemented by subscribing to member events, but there are several corner cases to consider. Therefore, this specific use case is made easily accessible by the Cluster Singleton in the contrib module.

Cluster Sharding
Distributes actors across several nodes in the cluster and supports interaction with the actors using their logical identifier, but without having to care about their physical location in the cluster.

See Cluster Sharding in the contrib module.

Distributed Publish Subscribe
Publish-subscribe messaging between actors in the cluster, and point-to-point messaging using the logical path of the actors, i.e. the sender does not have to know on which node the destination actor is running.

See Distributed Publish Subscribe in Cluster in the contrib module.

Cluster Client
Communication from an actor system that is not part of the cluster to actors running somewhere in the cluster. The client does not have to know on which node the destination actor is running.

See Cluster Client in the contrib module.

Failure Detector
In a cluster each node is monitored by a few (default maximum 5) other nodes, and when any of these detects the node as unreachable that information will spread to the rest of the cluster through the gossip. In other words, only one node needs to mark a node unreachable to have the rest of the cluster mark that node unreachable.

The failure detector will also detect if the node becomes reachable again. When all nodes that monitored the unreachable node detects it as reachable again the cluster, after gossip dissemination, will consider it as reachable.

If system messages cannot be delivered to a node it will be quarantined and then it cannot come back from unreachable. This can happen if the there are too many unacknowledged system messages (e.g. watch, Terminated, remote actor deployment, failures of actors supervised by remote parent). Then the node needs to be moved to the down or removed states and the actor system must be restarted before it can join the cluster again.

The nodes in the cluster monitor each other by sending heartbeats to detect if a node is unreachable from the rest of the cluster. The heartbeat arrival times is interpreted by an implementation of The Phi Accrual Failure Detector.

The suspicion level of failure is given by a value called phi. The basic idea of the phi failure detector is to express the value of phi on a scale that is dynamically adjusted to reflect current network conditions.

The value of phi is calculated as:

phi = -log10(1 - F(timeSinceLastHeartbeat))
where F is the cumulative distribution function of a normal distribution with mean and standard deviation estimated from historical heartbeat inter-arrival times.

In the Configuration you can adjust the akka.cluster.failure-detector.threshold to define when a phi value is considered to be a failure.

A low threshold is prone to generate many false positives but ensures a quick detection in the event of a real crash. Conversely, a high threshold generates fewer mistakes but needs more time to detect actual crashes. The default threshold is 8 and is appropriate for most situations. However in cloud environments, such as Amazon EC2, the value could be increased to 12 in order to account for network issues that sometimes occur on such platforms.

The following chart illustrates how phi increase with increasing time since the previous heartbeat.

../_images/phi11.png
Phi is calculated from the mean and standard deviation of historical inter arrival times. The previous chart is an example for standard deviation of 200 ms. If the heartbeats arrive with less deviation the curve becomes steeper, i.e. it is possible to determine failure more quickly. The curve looks like this for a standard deviation of 100 ms.

../_images/phi21.png
To be able to survive sudden abnormalities, such as garbage collection pauses and transient network failures the failure detector is configured with a margin, akka.cluster.failure-detector.acceptable-heartbeat-pause. You may want to adjust the Configuration of this depending on you environment. This is how the curve looks like for acceptable-heartbeat-pause configured to 3 seconds.

../_images/phi31.png
Death watch uses the cluster failure detector for nodes in the cluster, i.e. it detects network failures and JVM crashes, in addition to graceful termination of watched actor. Death watch generates the Terminated message to the watching actor when the unreachable cluster node has been downed and removed.

If you encounter suspicious false positives when the system is under load you should define a separate dispatcher for the cluster actors as described in Cluster Dispatcher.

Cluster Aware Routers
All routers can be made aware of member nodes in the cluster, i.e. deploying new routees or looking up routees on nodes in the cluster. When a node becomes unreachable or leaves the cluster the routees of that node are automatically unregistered from the router. When new nodes join the cluster, additional routees are added to the router, according to the configuration. Routees are also added when a node becomes reachable again, after having been unreachable.

There are two distinct types of routers.

Group - router that sends messages to the specified path using actor selection The routees can be shared among routers running on different nodes in the cluster. One example of a use case for this type of router is a service running on some backend nodes in the cluster and used by routers running on front-end nodes in the cluster.
Pool - router that creates routees as child actors and deploys them on remote nodes. Each router will have its own routee instances. For example, if you start a router on 3 nodes in a 10-node cluster, you will have 30 routees in total if the router is configured to use one instance per node. The routees created by the different routers will not be shared among the routers. One example of a use case for this type of router is a single master that coordinates jobs and delegates the actual work to routees running on other nodes in the cluster.
Router with Group of Routees
When using a Group you must start the routee actors on the cluster member nodes. That is not done by the router. The configuration for a group looks like this:

akka.actor.deployment {
  /statsService/workerRouter {
      router = consistent-hashing-group
      nr-of-instances = 100
      routees.paths = ["/user/statsWorker"]
      cluster {
        enabled = on
        allow-local-routees = on
        use-role = compute
      }
    }
}
Note
The routee actors should be started as early as possible when starting the actor system, because the router will try to use them as soon as the member status is changed to 'Up'.

The relative actor paths defined in routees.paths are used as for selecting the actors to which the messages will be forwarded to by the router. Messages will be forwarded to the routees using ActorSelection, so the same delivery semantics should be expected. It is possible to limit the lookup of routees to member nodes tagged with a certain role by specifying use-role.

nr-of-instances defines total number of routees in the cluster. Setting nr-of-instances to a high value will result in new routees added to the router when nodes join the cluster.

The same type of router could also have been defined in code:

import akka.cluster.routing.ClusterRouterGroup
import akka.cluster.routing.ClusterRouterGroupSettings
import akka.routing.ConsistentHashingGroup
 
val workerRouter = context.actorOf(
  ClusterRouterGroup(ConsistentHashingGroup(Nil), ClusterRouterGroupSettings(
    totalInstances = 100, routeesPaths = List("/user/statsWorker"),
    allowLocalRoutees = true, useRole = Some("compute"))).props(),
  name = "workerRouter2")
See Configuration section for further descriptions of the settings.

Router Example with Group of Routees
Let's take a look at how to use a cluster aware router with a group of routees, i.e. router sending to the paths of the routees.

The example application provides a service to calculate statistics for a text. When some text is sent to the service it splits it into words, and delegates the task to count number of characters in each word to a separate worker, a routee of a router. The character count for each word is sent back to an aggregator that calculates the average number of characters per word when all results have been collected.

Messages:

final case class StatsJob(text: String)
final case class StatsResult(meanWordLength: Double)
final case class JobFailed(reason: String)
The worker that counts number of characters in each word:

class StatsWorker extends Actor {
  var cache = Map.empty[String, Int]
  def receive = {
    case word: String =>
      val length = cache.get(word) match {
        case Some(x) => x
        case None =>
          val x = word.length
          cache += (word -> x)
          x
      }
 
      sender() ! length
  }
}
The service that receives text from users and splits it up into words, delegates to workers and aggregates:

class StatsService extends Actor {
  // This router is used both with lookup and deploy of routees. If you
  // have a router with only lookup of routees you can use Props.empty
  // instead of Props[StatsWorker.class].
  val workerRouter = context.actorOf(FromConfig.props(Props[StatsWorker]),
    name = "workerRouter")
 
  def receive = {
    case StatsJob(text) if text != "" =>
      val words = text.split(" ")
      val replyTo = sender() // important to not close over sender()
      // create actor that collects replies from workers
      val aggregator = context.actorOf(Props(
        classOf[StatsAggregator], words.size, replyTo))
      words foreach { word =>
        workerRouter.tell(
          ConsistentHashableEnvelope(word, word), aggregator)
      }
  }
}
 
class StatsAggregator(expectedResults: Int, replyTo: ActorRef) extends Actor {
  var results = IndexedSeq.empty[Int]
  context.setReceiveTimeout(3.seconds)
 
  def receive = {
    case wordCount: Int =>
      results = results :+ wordCount
      if (results.size == expectedResults) {
        val meanWordLength = results.sum.toDouble / results.size
        replyTo ! StatsResult(meanWordLength)
        context.stop(self)
      }
    case ReceiveTimeout =>
      replyTo ! JobFailed("Service unavailable, try again later")
      context.stop(self)
  }
}
Note, nothing cluster specific so far, just plain actors.

All nodes start StatsService and StatsWorker actors. Remember, routees are the workers in this case. The router is configured with routees.paths:

akka.actor.deployment {
  /statsService/workerRouter {
    router = consistent-hashing-group
    nr-of-instances = 100
    routees.paths = ["/user/statsWorker"]
    cluster {
      enabled = on
      allow-local-routees = on
      use-role = compute
    }
  }
}
This means that user requests can be sent to StatsService on any node and it will use StatsWorker on all nodes.

The Typesafe Activator tutorial named Akka Cluster Samples with Scala. contains the full source code and instructions of how to run the Router Example with Group of Routees.

Router with Pool of Remote Deployed Routees
When using a Pool with routees created and deployed on the cluster member nodes the configuration for a router looks like this:

akka.actor.deployment {
  /singleton/statsService/workerRouter {
      router = consistent-hashing-pool
      nr-of-instances = 100
      cluster {
        enabled = on
        max-nr-of-instances-per-node = 3
        allow-local-routees = on
        use-role = compute
      }
    }
}
It is possible to limit the deployment of routees to member nodes tagged with a certain role by specifying use-role.

nr-of-instances defines total number of routees in the cluster, but the number of routees per node, max-nr-of-instances-per-node, will not be exceeded. Setting nr-of-instances to a high value will result in creating and deploying additional routees when new nodes join the cluster.

The same type of router could also have been defined in code:

import akka.cluster.routing.ClusterRouterPool
import akka.cluster.routing.ClusterRouterPoolSettings
import akka.routing.ConsistentHashingPool
 
val workerRouter = context.actorOf(
  ClusterRouterPool(ConsistentHashingPool(0), ClusterRouterPoolSettings(
    totalInstances = 100, maxInstancesPerNode = 3,
    allowLocalRoutees = false, useRole = None)).props(Props[StatsWorker]),
  name = "workerRouter3")
See Configuration section for further descriptions of the settings.

Router Example with Pool of Remote Deployed Routees
Let's take a look at how to use a cluster aware router on single master node that creates and deploys workers. To keep track of a single master we use the Cluster Singleton in the contrib module. The ClusterSingletonManager is started on each node.

system.actorOf(ClusterSingletonManager.props(
  singletonProps = Props[StatsService], singletonName = "statsService",
  terminationMessage = PoisonPill, role = Some("compute")),
  name = "singleton")
We also need an actor on each node that keeps track of where current single master exists and delegates jobs to the StatsService. That is provided by the ClusterSingletonProxy.

system.actorOf(ClusterSingletonProxy.props(singletonPath = "/user/singleton/statsService",
  role = Some("compute")), name = "statsServiceProxy")
The ClusterSingletonProxy receives text from users and delegates to the current StatsService, the single master. It listens to cluster events to lookup the StatsService on the oldest node.

All nodes start ClusterSingletonProxy and the ClusterSingletonManager. The router is now configured like this:

akka.actor.deployment {
  /singleton/statsService/workerRouter {
    router = consistent-hashing-pool
    nr-of-instances = 100
    cluster {
      enabled = on
      max-nr-of-instances-per-node = 3
      allow-local-routees = on
      use-role = compute
    }
  }
}
The Typesafe Activator tutorial named Akka Cluster Samples with Scala. contains the full source code and instructions of how to run the Router Example with Pool of Remote Deployed Routees.

Cluster Metrics
The member nodes of the cluster can collect system health metrics and publish that to other cluster nodes and to the registered subscribers on the system event bus with the help of Cluster Metrics Extension.

How to Test
Multi Node Testing is useful for testing cluster applications.

Set up your project according to the instructions in Multi Node Testing and Multi JVM Testing, i.e. add the sbt-multi-jvm plugin and the dependency to akka-multi-node-testkit.

First, as described in Multi Node Testing, we need some scaffolding to configure the MultiNodeSpec. Define the participating roles and their Configuration in an object extending MultiNodeConfig:

import akka.remote.testkit.MultiNodeConfig
import com.typesafe.config.ConfigFactory
 
object StatsSampleSpecConfig extends MultiNodeConfig {
  // register the named roles (nodes) of the test
  val first = role("first")
  val second = role("second")
  val third = role("thrid")
 
  def nodeList = Seq(first, second, third)
 
  // Extract individual sigar library for every node.
  nodeList foreach { role ⇒
    nodeConfig(role) {
      ConfigFactory.parseString(s"""
      # Disable legacy metrics in akka-cluster.
      akka.cluster.metrics.enabled=off
      # Enable metrics extension in akka-cluster-metrics.
      akka.extensions=["akka.cluster.metrics.ClusterMetricsExtension"]
      # Sigar native library extract location during tests.
      akka.cluster.metrics.native-library-extract-folder=target/native/${role.name}
      """)
    }
  }
 
  // this configuration will be used for all nodes
  // note that no fixed host names and ports are used
  commonConfig(ConfigFactory.parseString("""
    akka.actor.provider = "akka.cluster.ClusterActorRefProvider"
    akka.remote.log-remote-lifecycle-events = off
    akka.cluster.roles = [compute]
     // router lookup config ...
    """))
 
}
Define one concrete test class for each role/node. These will be instantiated on the different nodes (JVMs). They can be implemented differently, but often they are the same and extend an abstract test class, as illustrated here.

// need one concrete test class per node
class StatsSampleSpecMultiJvmNode1 extends StatsSampleSpec
class StatsSampleSpecMultiJvmNode2 extends StatsSampleSpec
class StatsSampleSpecMultiJvmNode3 extends StatsSampleSpec
Note the naming convention of these classes. The name of the classes must end with MultiJvmNode1, MultiJvmNode2 and so on. It is possible to define another suffix to be used by the sbt-multi-jvm, but the default should be fine in most cases.

Then the abstract MultiNodeSpec, which takes the MultiNodeConfig as constructor parameter.

import org.scalatest.BeforeAndAfterAll
import org.scalatest.WordSpecLike
import org.scalatest.Matchers
import akka.remote.testkit.MultiNodeSpec
import akka.testkit.ImplicitSender
 
abstract class StatsSampleSpec extends MultiNodeSpec(StatsSampleSpecConfig)
  with WordSpecLike with Matchers with BeforeAndAfterAll
  with ImplicitSender {
 
  import StatsSampleSpecConfig._
 
  override def initialParticipants = roles.size
 
  override def beforeAll() = multiNodeSpecBeforeAll()
 
  override def afterAll() = multiNodeSpecAfterAll()
Most of this can of course be extracted to a separate trait to avoid repeating this in all your tests.

Typically you begin your test by starting up the cluster and let the members join, and create some actors. That can be done like this:

"illustrate how to startup cluster" in within(15 seconds) {
  Cluster(system).subscribe(testActor, classOf[MemberUp])
  expectMsgClass(classOf[CurrentClusterState])
 
  val firstAddress = node(first).address
  val secondAddress = node(second).address
  val thirdAddress = node(third).address
 
  Cluster(system) join firstAddress
 
  system.actorOf(Props[StatsWorker], "statsWorker")
  system.actorOf(Props[StatsService], "statsService")
 
  receiveN(3).collect { case MemberUp(m) => m.address }.toSet should be(
    Set(firstAddress, secondAddress, thirdAddress))
 
  Cluster(system).unsubscribe(testActor)
 
  testConductor.enter("all-up")
}
From the test you interact with the cluster using the Cluster extension, e.g. join.

Cluster(system) join firstAddress
Notice how the testActor from testkit is added as subscriber to cluster changes and then waiting for certain events, such as in this case all members becoming 'Up'.

The above code was running for all roles (JVMs). runOn is a convenient utility to declare that a certain block of code should only run for a specific role.

"show usage of the statsService from one node" in within(15 seconds) {
  runOn(second) {
    assertServiceOk()
  }
 
  testConductor.enter("done-2")
}
 
def assertServiceOk(): Unit = {
  val service = system.actorSelection(node(third) / "user" / "statsService")
  // eventually the service should be ok,
  // first attempts might fail because worker actors not started yet
  awaitAssert {
    service ! StatsJob("this is the text that will be analyzed")
    expectMsgType[StatsResult](1.second).meanWordLength should be(
      3.875 +- 0.001)
  }
 
}
Once again we take advantage of the facilities in testkit to verify expected behavior. Here using testActor as sender (via ImplicitSender) and verifing the reply with expectMsgPF.

In the above code you can see node(third), which is useful facility to get the root actor reference of the actor system for a specific role. This can also be used to grab the akka.actor.Address of that node.

val firstAddress = node(first).address
val secondAddress = node(second).address
val thirdAddress = node(third).address
JMX
Information and management of the cluster is available as JMX MBeans with the root name akka.Cluster. The JMX information can be displayed with an ordinary JMX console such as JConsole or JVisualVM.

From JMX you can:

see what members that are part of the cluster
see status of this node
see roles of each member
join this node to another node in cluster
mark any node in the cluster as down
tell any node in the cluster to leave
Member nodes are identified by their address, in format akka.<protocol>://<actor-system-name>@<hostname>:<port>.

Command Line Management
The cluster can be managed with the script bin/akka-cluster provided in the Akka distribution.

Run it without parameters to see instructions about how to use the script:

Usage: bin/akka-cluster <node-hostname> <jmx-port> <command> ...
 
Supported commands are:
           join <node-url> - Sends request a JOIN node with the specified URL
          leave <node-url> - Sends a request for node with URL to LEAVE the cluster
           down <node-url> - Sends a request for marking node with URL as DOWN
             member-status - Asks the member node for its current status
                   members - Asks the cluster for addresses of current members
               unreachable - Asks the cluster for addresses of unreachable members
            cluster-status - Asks the cluster for its current status (member ring,
                             unavailable nodes, meta data etc.)
                    leader - Asks the cluster who the current leader is
              is-singleton - Checks if the cluster is a singleton cluster (single
                             node cluster)
              is-available - Checks if the member node is available
Where the <node-url> should be on the format of
  'akka.<protocol>://<actor-system-name>@<hostname>:<port>'
 
Examples: bin/akka-cluster localhost 9999 is-available
          bin/akka-cluster localhost 9999 join akka.tcp://MySystem@darkstar:2552
          bin/akka-cluster localhost 9999 cluster-status
To be able to use the script you must enable remote monitoring and management when starting the JVMs of the cluster nodes, as described in Monitoring and Management Using JMX Technology

Example of system properties to enable remote monitoring and management:

java -Dcom.sun.management.jmxremote.port=9999 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false
Configuration
There are several configuration properties for the cluster. We refer to the reference configuration for more information.

Cluster Info Logging
You can silence the logging of cluster events at info level with configuration property:

akka.cluster.log-info = off
Cluster Dispatcher
Under the hood the cluster extension is implemented with actors and it can be necessary to create a bulkhead for those actors to avoid disturbance from other actors. Especially the heartbeating actors that is used for failure detection can generate false positives if they are not given a chance to run at regular intervals. For this purpose you can define a separate dispatcher to be used for the cluster actors:

akka.cluster.use-dispatcher = cluster-dispatcher
 
cluster-dispatcher {
  type = "Dispatcher"
  executor = "fork-join-executor"
  fork-join-executor {
    parallelism-min = 2
    parallelism-max = 4
  }
}
