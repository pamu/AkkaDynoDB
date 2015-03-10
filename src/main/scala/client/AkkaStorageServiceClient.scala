package client

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{ReachabilityEvent, MemberEvent}
import scala.concurrent.duration._

/**
 * Created by android on 10/3/15.
 */

class AkkaStorageServiceClient(servicePath: String) extends Actor with ActorLogging {
  import worker._

  val cluster = Cluster(context.system)

  val servicePathElements = servicePath match {
    case RelativeActorPath(elements) => elements
    case _ => throw new IllegalArgumentException(
      "servicePath [%s] is not a valid relative error path" format servicePath
    )
  }

  override def preStart(): Unit = {
    cluster.subscribe(self, classOf[MemberEvent], classOf[ReachabilityEvent])
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  import context.dispatcher
  val tickTask = context.system.scheduler.schedule(2.seconds, 2.seconds, self, "tick")

  var nodes = Set.empty[Address]

  override def receive = {
    case "tick" if nodes.isEmpty => {

    }
  }
}

object Starter {
  def main(args: Array[String]): Unit = {
    //start the ass client

  }
}