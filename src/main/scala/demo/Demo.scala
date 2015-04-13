package demo

import akka.actor.{ActorLogging, Actor}

/**
import akka.actor.Actor

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by pamu on 1/4/15.
 */

object Demo {
  final case class Entry[M](tableQuery: TableQuery[Table[M]], model: M)
  final case class Get[M](tableQuery: TableQuery[Table[M]], id: Long)
  final case class Evict[M](tableQuery: TableQuery[Table[M]], id: Long)
}

class Demo extends Actor {
  import Demo._
  import database.Db._
  def receive = {
    case Entry(tableQuery, model) => {
      db.withSession {
        implicit sx => tableQuery += model
      }
    }
    case Get(tableQuery, id) => {
      db.withSession {
        implicit sx => {
          val query = for(model <- tableQuery.filter(_.id === id)) yield model
          query.firstOption
        }
      }
    }
    case Evict(tableQuery, id) => {
      db.withSession {
        implicit sx => {
          val query = for(model <- tableQuery.filter(_.id === id)) yield model
          query.delete
        }
      }
    }
  }
}

object Starter {
  def main(args: Array[String]): Unit = {
    println("Starter")
  }
}
  object Greeter {
  case object Hi
  case object Hello
}

class Greeter extends Actor with ActorLogging {
  import Greeter._
  override def receive = {
    case Hi => sender() ! Hello
    case _ => log.info("unknown message")
  }
}**/


