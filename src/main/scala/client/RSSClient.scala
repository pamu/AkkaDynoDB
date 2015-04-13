package client

import akka.actor._
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent._
import storage.StorageNode.{Entry, Message}

//import database.tableQueries.TableWithIdQuery
//import database.tables.IdTable

import scala.concurrent.duration._
//import scala.concurrent.forkjoin.ThreadLocalRandom

/**
 * Created by android on 10/3/15.
 */

object RSSClient {
  trait Result
  final case class Error(msg: String) extends Result
  final case class Success(value: Any) extends Result
}

class RSSClient(servicePath: String) extends Actor with ActorLogging {

  import RSSClient._

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
    //tickTask.cancel()
  }

  import context.dispatcher

  val tickTask = context.system.scheduler.schedule(2.seconds, 10.seconds, self, "tick")

  var nodes = Set.empty[Address]

  var seq = 0

  /*
  import scala.slick.driver.MySQLDriver.simple._

  case class User(name: String, id: Option[Long] = None)

  class Users(tag: Tag) extends IdTable[User, Long](tag, "users") {
    def name = column[String]("name")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, id.?) <> (User.tupled, User.unapply)
  }

  val users = new TableWithIdQuery[User, Long, Users](tag => new Users(tag)) {
    /**
     * Extracts the model Id of a arbitrary model.
     * @param model a mapped model
     * @return a Some[I] if Id is filled, None otherwise
     */
    override def extractId(model: User): Option[Long] = model.id

    /**
     *
     * @param model a mapped model (usually without an assigned id).
     * @param id an id, usually generate by the database
     * @return a model M with an assigned id.
     */
    override def withId(model: User, id: Long): User = model.copy(id = Some(id))
  }**/

  override def receive = {

    case "tick" if !nodes.isEmpty => {

    /**
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
      **/
      log.info(s"${nodes.toIndexedSeq.size} nodes registered")
      self ! Entry(seq, "Pamu Nagarjuna")
      seq += 1
      log.info("{} sent.", Entry(1, "Pamu Nagarjuna"))

      //self ! storage.StorageNode.Entry[User, Long, Users](users, 1L, User("pamu nagarjuna"))
    }

    case message: Message =>
      val address = nodes.toIndexedSeq(0)
      val service = context.actorSelection(RootActorPath(address) / servicePathElements)
      service ! message

    case result: Result =>
      result match {
        case Error(msg) => log.info(msg)
        case Success(value) =>
          value match {
            case msg: String => log.info(msg)
            case value => log.info("{}", value)
          }
      }

    case state: CurrentClusterState => nodes = state.members.collect {
        case member if member.hasRole("storage") && member.status == MemberStatus.Up => member.address
      }

    case MemberUp(member) if member.hasRole("storage") => nodes += member.address
    case other: MemberEvent                            => nodes -= other.member.address
    case UnreachableMember(member)                     => nodes -= member.address
    case ReachableMember(member)                       => nodes += member.address

    case any => log.info("unknown message of type: {}", any getClass)
  }
}

// Wrapper object for the Cluster bootstrap code
object Starter {
  def main(args: Array[String]): Unit = {
    val system = ActorSystem("ClusterSystem")
    system.actorOf(Props(classOf[RSSClient], "/user/ReactiveStorageService"), "client")
  }
}