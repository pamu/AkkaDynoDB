package schema

/**
 * Created by pnagarjuna on 21/05/15.
 */

import database.tables.IdTable

import scala.slick.driver.MySQLDriver.simple._

class Entities(tag: Tag) extends IdTable[Entity, Long](tag, "Entities") {
  def key = column[String]("key", O.NotNull)
  def value = column[String]("value", O.NotNull)
  def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def * = (key, value, id.?) <> (Entity.tupled, Entity.unapply)
}


