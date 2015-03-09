package worker

import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import com.typesafe.config.ConfigFactory

/**
 * Created by android on 8/3/15.
 */
object Worker {
  final case class Evict(key: String)
  final case class Get(key: String)
  final case class Entry(key: String, value: String)
}

class Worker extends Actor with ActorLogging {

  var cache = Map.empty[String, String]

  val cluster = Cluster(context.system)

  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])
  override def postStop(): Unit = cluster.unsubscribe(self)

  import Worker._

  override def receive = {
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).
      foreach(x => println(x.address + " is Up"))
    case MemberUp(member) => log.info("member {} is up", member.address)

    case Entry(key, value) => cache += (key -> value)
    case Get(key) => sender() ! cache.get(key)
    case Evict(key) => cache -= key
  }
}


object Starter {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [store]")).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[Worker], name = "store")
  }
}