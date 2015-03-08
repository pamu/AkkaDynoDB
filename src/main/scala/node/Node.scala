package node

import akka.actor.{ActorLogging, Actor}
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}

/**
 * Created by android on 8/3/15.
 */
object Node {
  final case class Evict(key: String)
  final case class Get(key: String)
  final case class Entry(key: String, value: String)
}

class Node extends Actor with ActorLogging {

  var cache = Map.empty[String, String]

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  import Node._

  override def receive = {
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).foreach(println)
    case MemberUp(member) => log.info("member {} is up", member.address)

    case Entry(key, value) => cache += (key -> value)
    case Get(key) => sender() ! cache.get(key)
    case Evict(key) => cache -= key
  }
}

object NodeBootstrap {

}