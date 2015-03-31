package database

/**
import java.sql.Timestamp

import scala.slick.driver.MySQLDriver.simple._
**/

/**
 * Created by android on 11/3/15.
 */

object Tables {
  /**
  case class User(firstName: String, lastName: String, email: String,
                  time: Timestamp, id: Option[Long] = None)
  class Users(tag: Tag) extends Table[User](tag, "users") {
    def firstName = column[String]("firstName", O.NotNull)
    def lastName = column[String]("lastName", O.NotNull)
    def email = column[String]("email", O.NotNull)
    def time = column[Timestamp]("timestamp", O.NotNull)
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (firstName, lastName, email, time, id.?) <> (User.tupled, User.unapply)
  }
  val users = TableQuery[Users] **/
}
