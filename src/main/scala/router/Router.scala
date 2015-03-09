package router

import akka.actor.{ActorRef, Props, ActorSystem}
import akka.routing.{ConsistentHashingGroup, FromConfig}
import com.typesafe.config.ConfigFactory
import node.Node

/**
 * Created by android on 8/3/15.
 */
object Router {
  def main(args: Array[String]): Unit = {
    val paths = List("akka.tcp://NodeSystem@127.0.0.1:2551",
      "akka.tcp://NodeSystem@127.0.0.1:2552")
    val router: ActorRef = ActorSystem("NodeSystem").
      actorOf(ConsistentHashingGroup(paths).props(), "router")

  }
}
