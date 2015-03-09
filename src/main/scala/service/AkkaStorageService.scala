package service

import akka.actor.{Props, ActorSystem, Actor}
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import worker.Worker

/**
 * Created by android on 10/3/15.
 */

class AkkaStorageService extends Actor {

  val workerRouter = context.actorOf(FromConfig.props(Props[Worker]), name = "worker")

  import Worker._

  override def receive = {
    case Get(key) => {

    }
    case Entry(key, value) => {

    }
    case Evict(key) => {

    }
  }
}

object Starter {
  def main(args: Array[String]): Unit = {
    val port = if(args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").withFallback(
      ConfigFactory.parseString("akka.cluster.roles = [store]")).withFallback(
        ConfigFactory.load("ass"))

    val system = ActorSystem("ClusterSystem", config)

    system.actorOf(Props[Worker], name = "worker")
    system.actorOf(Props[AkkaStorageService], name = "akkaStorageService")
  }
}