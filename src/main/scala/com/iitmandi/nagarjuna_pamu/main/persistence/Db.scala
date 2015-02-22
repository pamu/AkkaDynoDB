package com.iitmandi.nagarjuna_pamu.main.persistence

/**
 * Created by android on 31/1/15.
 */

import scala.slick.driver.MySQLDriver.simple._;

object Db {
  lazy val db = Database.forURL(url = "", user = "", password = "", driver = "")
}
