package db

/**
 * Created by android on 8/3/15.
 */

import scala.slick.driver.MySQLDriver.simple._

object DB {
  val db = Database.forURL(url = "", driver  = "", user = "", password = "")
}
