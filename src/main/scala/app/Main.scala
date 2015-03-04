package app

import akka.actor.{Props, ActorRef, ActorSystem}
import akka.routing.ConsistentHashingPool
import akka.routing.ConsistentHashingRouter._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

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

    store ! ConsistentHashableEnvelope(
      message = Entry("msg2", "hello"), hashKey = "msg2"
    )

    import scala.concurrent.duration._

    implicit val timeout = Timeout(5 seconds)
    val future = store ? Get("msg2")

    future onComplete {
      case Success(x) => println(x)
      case Failure(t) => println(t)
    }

    Await.result(future, Duration.Inf)
  }
}
