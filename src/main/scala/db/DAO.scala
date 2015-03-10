package db

/**
 * Created by android on 11/3/15.
 */

import scala.slick.driver.MySQLDriver.simple._

/**
 *
 */
object DAO {

  /**
   *
   */
  def create = DB.db.withSession(implicit sx => {
    Tables.users.ddl.create
  })

}
