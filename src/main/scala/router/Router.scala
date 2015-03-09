package router

import akka.actor.{ActorRef, ActorSystem}
import akka.routing.ConsistentHashingGroup
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
