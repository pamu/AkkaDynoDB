package db

/**
 * Created by android on 11/3/15.
 */

import scala.slick.driver.MySQLDriver.simple._
import Tables._
/**
 *
 */
object DAO {

  /**
   * create the table
   */
  def create = DB.db.withSession(implicit sx => {
    Tables.users.ddl.create
  })

  /**
   * save the user object to the database
   * @param user
   */
  def save(user: User): Unit = DB.db.withSession(implicit sx => {
    Tables.users += user
  })

}
