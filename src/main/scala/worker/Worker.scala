package worker

import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import com.typesafe.config.ConfigFactory

/**
 * Created by android on 8/3/15.
 */

/** Worker object wrapping the cluster node messages */
object Worker {

  /**Evict message removes the entry with key
   *
   * @param key
   */
  final case class Evict(key: String)

  /**Get fetches the value of the Entry with given key
   *
   * @param key
   */
  final case class Get(key: String)

  /**Entry adds the Entry to the existing map
   *
   * @param key
   * @param value
   */
  final case class Entry(key: String, value: String)
}

/** Worker Actor */
class Worker extends Actor with ActorLogging {

  /** Storage data structure */
  var cache = Map.empty[String, String]

  /** cluster ref */
  val cluster = Cluster(context.system)

  // on actor pre start
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  // on actor post stop
  override def postStop(): Unit = cluster.unsubscribe(self)

  import Worker._

  /**
   * Receive method of the actor
   * @return
   */
  override def receive = {

    /**
     * Cluster state
     */
    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).
      foreach(x => log.info(s"${x.address} is Up"))

    /**
     * Member up event
     */
    case MemberUp(member) => log.info("member {} is up", member.address)


    /**
     * Entry message
     */
    case Entry(key, value) =>
      cache += (key -> value)
      log.info(s"entry request ${Entry(key, value)} => [${cache.mkString(", ")}]")

    /**
     * Get message
     */
    case Get(key) =>
      sender() ! cache.get(key)
      log.info(s"get request ${Get(key)} => [${cache.mkString(", ")}]")

    /**
     * Evict message
     */
    case Evict(key) =>
      cache -= key
      log.info(s"evict request ${Evict(key)} => [${cache.mkString(", ")}]")

  }

}


/**
 * Starter Object
 */
object Starter {

  /**
   * Main method
   * @param args
   */
  def main(args: Array[String]): Unit = {

    /**
     * Use random port if port is not provided as the commandline argument
     */
    val port = if (args.isEmpty) "0" else args(0)

    /**
     * Read the configuration from application.conf
     */
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [worker]")).
      withFallback(ConfigFactory.load())

    /**
     * actor system names cluster system
     */
    val system = ActorSystem("ClusterSystem", config)

    /**
     * Worker node
     */
    system.actorOf(Props[Worker], name = "worker")

  }

}