package app

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.routing.ConsistentHashingPool
import akka.routing.ConsistentHashingRouter._

/**
 * Created by android on 3/3/15.
 */
object Main {
  def main(args: Array[String]): Unit = {

    println("Hello world")
    
    val system = ActorSystem("system")

    import DataStore._

    def hashMapping: ConsistentHashMapping = {
      case Evict(key) => key
    }

    val store: ActorRef = system.actorOf(ConsistentHashingPool(10, hashMapping = hashMapping).
      props(Props[DataStore]), name = "DataStore")

    store ! ConsistentHashableEnvelope(
      message = Entry("msg1", "hi"), hashKey = "msg1"
    )

    store ! ConsistentHashableEnvelope(
      message = Entry("msg2", "hello"), hashKey = "msg2"
    )

    store ! Get("msg1")

  }
}
