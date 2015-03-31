package demo

import akka.actor.Actor

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by pamu on 1/4/15.
 */

object Demo {
  final case class Save[M](tableQuery: TableQuery[Table[M]], model: M)
}

class Demo extends Actor {
  import Demo._
  def receive = {
    case Save(tableQuery, model) =>
  }
}
