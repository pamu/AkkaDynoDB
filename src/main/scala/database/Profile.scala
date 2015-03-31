package database

import scala.slick.driver.JdbcDriver

trait Profile {
  val jdbcDriver: JdbcDriver
}
