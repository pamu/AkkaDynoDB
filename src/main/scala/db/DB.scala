package db

/**
 * Created by android on 8/3/15.
 */

import scala.slick.driver.MySQLDriver.simple._

/**DB object to wrap the database handling code */
object DB {

  /**Database.forURL method returns database handle taking
   * url, driver, user, password as parameters
   *
   * database handle to drive db queries
   */
  lazy val db = Database.forURL(
    url = "jdbc:mysql://localhost/democracy_db",
    driver = "com.mysql.jdbc.Driver",
    user="root",
    password="root")

}
