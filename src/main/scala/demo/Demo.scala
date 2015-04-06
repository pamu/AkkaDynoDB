package demo

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
}**/