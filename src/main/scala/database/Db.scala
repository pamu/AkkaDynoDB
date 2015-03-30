package database

/**
 * Created by android on 8/3/15.
 */

import scala.slick.driver.MySQLDriver.simple._

object Db {
  lazy val db = Database.forURL(
    url = "jdbc:mysql://localhost/democracy_db",
    driver = "com.mysql.jdbc.Driver",
    user="root",
    password="root")
}
