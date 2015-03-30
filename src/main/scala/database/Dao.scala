package database

/**
 * Created by android on 11/3/15.
 */

import database.Tables._

import scala.slick.driver.MySQLDriver.simple._

object Dao {
  def create = Db.db.withSession(implicit sx => {
    Tables.users.ddl.create
  })
  def save(user: User): Unit = Db.db.withSession(implicit sx => {
    Tables.users += user
  })
}
