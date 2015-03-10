package client

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.cluster.protobuf.msg.ClusterMessages.MemberStatus
import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom

/**
 * Created by android on 10/3/15.
 */

/**Akka Storage Service Client Actor
 *
 * @param servicePath
 */
class AkkaStorageServiceClient(servicePath: String) extends Actor with ActorLogging {

  /** Cluster ref to subscribe to cluster events
   * cluster companion object takes context.system
   */
  val cluster = Cluster(context.system)

  /** Service Path is the path of the
   * Akka Storage Service
   */
  val servicePathElements = servicePath match {

    case RelativeActorPath(elements) => elements

    case _ => throw new IllegalArgumentException(
      "servicePath [%s] is not a valid relative error path" format servicePath
    )

  }

  /** preStart method
   *  executed in the beginning of the actor formation
   */
  override def preStart(): Unit = {
    /**
     * Subscribing to the cluster events
     */
    cluster.subscribe(self, classOf[MemberEvent], classOf[ReachabilityEvent])
  }

  /**
   * postStop method is executed just after actor is stopped
   */
  override def postStop(): Unit = {
    /**
     * unsubscribe to the cluster events
     */
    cluster.unsubscribe(self)

    /**
     * cancel the ticks once the actor stopped
     */
    tickTask.cancel()
  }

  import context.dispatcher

  /**
   * start ticks with a delay of 2 seconds and 10 seconds of delay between 2 ticks
   */
  val tickTask = context.system.scheduler.schedule(2.seconds, 10.seconds, self, "tick")

  /**
   * Set containing registered node address (these nodes are Akka Storage System instances)
   */
  var nodes = Set.empty[Address]

  /**
   * Integer seq
   */
  var seq = 0

  /**
   * Receive method overridden
   * @return
   */
  override def receive = {

    case "tick" if !nodes.isEmpty => {

      log.info("sending request")

      //pick the random Akka Storage Service Instance
      val address = nodes.toIndexedSeq(ThreadLocalRandom.current.nextInt(nodes.size))

      //
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)

      //val random = ThreadLocalRandom.current().nextInt(1, 10)
      seq += 1

      log.info(nodes.mkString)

      log.info(s"Generated seq number $seq and hitting node {}", service)

      val random = seq

      if(seq % 2 == 0) {

        log.info("{}", worker.Worker.Entry(s"key$random", s"value$random").toString)

        service ! worker.Worker.Entry(s"key$random", s"value$random")

      } else {

        log.info("{}", worker.Worker.Entry(s"key$random", s"value$random").toString)

        service ! worker.Worker.Get(s"name$random")

      }

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

/**
 * Wraper object for the Cluster bootstrap code
 *
 */
object Starter {

  /**
   * @param args
   */
  def main(args: Array[String]): Unit = {

    /**
     * Getting the actor system by the name `ClusterSystem`
     */
    val system = ActorSystem("ClusterSystem")

    //start the client actor

    system.actorOf(Props(classOf[AkkaStorageServiceClient], "/user/akkaStorageService"), "client")

  }
}