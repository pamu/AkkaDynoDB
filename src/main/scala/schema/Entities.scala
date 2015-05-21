package schema

/**
 * Created by pnagarjuna on 21/05/15.
 */

import database.tables.IdTable

import scala.slick.driver.MySQLDriver.simple._

class Entities(tag: Tag) extends IdTable[Entity, Long](tag, "Entities") {
  def json = column[String]("json", O.NotNull)
  def id = column[Long]("id", O.PrimaryKey)
  def * = (json, id) <> (Entity.tupled, Entity.unapply)
}


