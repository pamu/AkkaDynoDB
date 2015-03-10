package client

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.protobuf.msg.ClusterMessages.MemberStatus
import worker.Worker.{Get, Entry}
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

/**
 * Created by android on 10/3/15.
 */

class AkkaStorageServiceClient(servicePath: String) extends Actor with ActorLogging {

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
    tickTask.cancel()
  }

  import context.dispatcher
  val tickTask = context.system.scheduler.schedule(2.seconds, 2.seconds, self, "tick")

  var nodes = Set.empty[Address]

  override def receive = {
    case "tick" if nodes.isEmpty => {
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      val random = ThreadLocalRandom.current().nextInt(1, 10)
      if(random % 2 == 0)
        service ! worker.Worker.Entry(s"name$random", s"pamu$random")
      else
        service ! worker.Worker.Get(s"name$random")
    }
    case state: CurrentClusterState =>
      nodes = state.members.collect{
        case member if member.hasRole("worker") && member.status == MemberStatus.Up => member.address
      }
    case MemberUp(member) if member.hasRole("worker") => nodes += member.address
    case other: MemberEvent                           => nodes -= other.member.address
    case UnreachableMember(member)                    => nodes -= member.address
    case ReachableMember(member)                      => nodes += member.address
  }
}

/** Wraper object for the Cluster bootstrap code */
object Starter {
  def main(args: Array[String]): Unit = {
    /**Getting the actor system by the name `ClusterSystem` */
    val system = ActorSystem("ClusterSystem")
    //start the client actor
    system.actorOf(Props(classOf[AkkaStorageServiceClient], "/user/akkaStorageService"), "client")
  }
}