package storage

import akka.actor.{Props, ActorSystem, ActorLogging, Actor}
import akka.cluster.{MemberStatus, Cluster}
import akka.cluster.ClusterEvent.{CurrentClusterState, MemberUp}
import com.typesafe.config.ConfigFactory
import constants.Constants
//import database.tableQueries.TableWithIdQuery
//import database.tables.IdTable
//import models.Identifiable

import scala.concurrent.Future
//import scala.slick.driver.MySQLDriver.simple._

import akka.pattern.pipe

import scala.util.{Failure, Success}

/**
 * Created by android on 8/3/15.
 */

object StorageNode {
  /**
  trait DbMessage
  final case class Entry[M, I: BaseColumnType, T <: IdTable[M, I]](tableWithIdQuery: TableWithIdQuery[M, I, T], key: I, model: M) extends DbMessage
  final case class Evict[M, I: BaseColumnType, T <: IdTable[M, I]](tableWithIdQuery: TableWithIdQuery[M, I, T], key: I) extends DbMessage
  final case class Get[M, I: BaseColumnType, T <: IdTable[M, I]](tableWithIdQuery: TableWithIdQuery[M, I, T], key: I) extends DbMessage**/
  trait Message extends Serializable {
    val key: String
  }
  final case class Entry(override val key: String, value: Any) extends Message
  final case class Evict(override val key: String) extends Message
  final case class Get(override val key: String) extends Message
  final case class All(override val key: String) extends Message
}


class StorageNode extends Actor with ActorLogging {

  var cache = scala.collection.immutable.ListMap.empty[String, Any]

  /**
  lazy val db = Database.forURL(
    url = s"jdbc:mysql://localhost/demo${cluster.selfAddress.hostPort}",
    driver = "com.mysql.jdbc.Driver",
    user="root",
    password="root")**/

  val cluster = Cluster(context.system)

  // on actor pre start
  override def preStart(): Unit = cluster.subscribe(self, classOf[MemberUp])

  // on actor post stop
  override def postStop(): Unit = cluster.unsubscribe(self)

  import StorageNode._
  import client.RSSClient

  //import context.dispatcher

  override def receive = {

    case state: CurrentClusterState => state.members.filter(_.status == MemberStatus.Up).
      foreach(x => log.info(s"${x.address} is Up"))

    case MemberUp(member) => log.info("member {} is up", member.address)

    case Entry(key, value) =>
      cache += (key -> value)
      sender ! RSSClient.Success(s"[success]::> ${Entry(key, value).toString} successful.")
      log.info("{}", cache.mkString("\n", "\n", "\n"))
      /**
      val client = sender()
      val future = Future {
        db.withSession { implicit sx =>
          tableWithIdQuery.createIfNotExists
          tableWithIdQuery.save(model)
          client ! "Done"
        }
      }
      future pipeTo self
      //cache += (key -> value)
      //log.info(s"entry request ${Entry(key, value)} => [${cache.mkString(", ")}]")
        **/

    case Get(key) =>
      log.info("{}", Get(key))
      if (cache contains key) {
        sender ! RSSClient.Success(cache(key))
      } else {
        sender ! RSSClient.Error(s"[failure]::> key $key not found")
      }
      /**
      val client = sender()
      val future = Future {
        db.withSession { implicit sx =>
          tableWithIdQuery.tryFindById(key) match {
            case Success(model) => client ! model.toString
            case Failure(t) => client ! s"couldn't find because ${t.getMessage}"
          }
        }
      }

      future pipeTo self
      //sender() ! cache.get(key)
      //log.info(s"get request ${Get(key)} => [${cache.mkString(", ")}]")
        **/
    case Evict(key) =>
      log.info("{}", Evict(key))
      if (cache contains key) {
	      cache -= key
        sender() ! RSSClient.Success(s"key $key Successfully deleted.")
      } else {
        sender() ! RSSClient.Error(s"key $key not found. ")
      }
      /**
      val client = sender()
      val future = Future {
        db.withSession { implicit sx =>
          tableWithIdQuery.deleteById(key)
          client ! "Delete Successful"
        }
      }

      future pipeTo self
      //cache -= key
      //log.info(s"evict request ${Evict(key)} => [${cache.mkString(", ")}]")
        **/
     case All(key) => 
       log.info("Requesting all keys")
       if (cache contains key)
         sender ! RSSClient.Success(cache.mkString("\n", "\n", "\n"))
       else sender ! RSSClient.Error(s"key $key not found.")
  }
}


object Starter {
  def main(args: Array[String]): Unit = {
    val port = if (args.isEmpty) "0" else args(0)
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port").
      withFallback(ConfigFactory.parseString("akka.cluster.roles = [storage]")).
      withFallback(ConfigFactory.load())
    val system = ActorSystem("ClusterSystem", config)
    system.actorOf(Props[StorageNode], name = Constants.StorageNode)
  }
}


