package replication

import akka.actor.{Actor, ActorLogging}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import akka.cluster.{Cluster, MemberStatus}
import storage.StorageNode

/**
 * Created by pnagarjuna on 27/05/15.
 */

class Replicator extends Actor with ActorLogging {
  var cache = scala.collection.immutable.ListMap.empty[String, Any]

  val cluster = Cluster(context.system)

  // on actor pre start
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  // on actor post stop
  override def postStop(): Unit = cluster.unsubscribe(self)

  import StorageNode._

  override def receive = {

    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).
      foreach(x => log.info(s"${x.address} is Up"))

    case MemberUp(member) => log.info("member {} is up", member.address)

    case Entry(key, value) =>
      cache += (key -> value)
      log.info("{}", cache.mkString("\n", "\n", "\n"))

    case Get(key) =>
      log.info("{}", Get(key))

    case Evict(key) =>
      log.info("{}", Evict(key))
      if (cache contains key) {
        cache -= key
      }
    case x => log.info("unknown message {} of type {}", x, x getClass)
  }
}
