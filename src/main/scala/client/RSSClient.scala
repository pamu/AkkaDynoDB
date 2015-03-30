package client

import akka.actor._
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent._
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

/**
 * Created by android on 10/3/15.
 */


class RSSClient(servicePath: String) extends Actor with ActorLogging {

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
  val tickTask = context.system.scheduler.schedule(2.seconds, 10.seconds, self, "tick")
  var nodes = Set.empty[Address]
  var seq = 0

  override def receive = {

    case "tick" if !nodes.isEmpty => {
      log.info("sending request")
      //pick the random Akka Storage Service Instance
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      //val random = ThreadLocalRandom.current().nextInt(1, 10)
      seq += 1
      log.info(nodes.mkString)
      log.info(s"Generated seq number $seq and hitting node {}", service)
      val random = seq
      if(1 == 0) {

        log.info("{}", storage.StorageNode.Entry(s"key$random", s"value$random").toString)

        service ! storage.StorageNode.Entry(s"key$random", s"value$random")

      } else {

        log.info("{}", storage.StorageNode.Entry(s"key$random", s"value$random").toString)

        service ! storage.StorageNode.Get(s"name$random")
      }
    }
    case state: CurrentClusterState =>
      nodes = state.members.collect {
        case member if member.hasRole("storage") && member.status == MemberStatus.Up => member.address
      }
    case MemberUp(member) if member.hasRole("storage") => nodes += member.address
    case other: MemberEvent                           => nodes -= other.member.address
    case UnreachableMember(member)                    => nodes -= member.address
    case ReachableMember(member)                      => nodes += member.address
  }
}

// Wrapper object for the Cluster bootstrap code
object Starter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[RSSClient], "/user/akkaStorageService"), "client")
  }
}