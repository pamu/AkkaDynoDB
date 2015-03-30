package storage

import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import com.typesafe.config.ConfigFactory

/**
 * Created by android on 8/3/15.
 */

object StorageNode {
  final case class Evict(key: String)
  final case class Get(key: String)
  final case class Entry(key: String, value: String)
}


class StorageNode extends Actor with ActorLogging {

  var cache = Map.empty[String, String]
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
      log.info(s"entry request ${Entry(key, value)} => [${cache.mkString(", ")}]")
    case Get(key) =>
      sender() ! cache.get(key)
      log.info(s"get request ${Get(key)} => [${cache.mkString(", ")}]")
    case Evict(key) =>
      cache -= key
      log.info(s"evict request ${Evict(key)} => [${cache.mkString(", ")}]")
  }
}


object Starter {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [storage]")).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[StorageNode], name = "storageNode")
  }
}