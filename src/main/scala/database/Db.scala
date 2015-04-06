package database

/**
 * Created by android on 8/3/15.
 */

import akka.actor.{Props, ActorSystem, Actor}
import database.tableQueries.TableWithIdQuery
import database.tables.IdTable

import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta.MTable

object Db {

  lazy val db = Database.forURL(
    url = "jdbc:mysql://localhost/demo",
    driver = "com.mysql.jdbc.Driver",
    user="root",
    password="root")

  case class User(name: String, id: Option[Long] = None)

  class Users(tag: Tag) extends IdTable[User, Long](tag, "users") {
    def name = column[String]("name")
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
    def * = (name, id.?) <> (User.tupled, User.unapply)
  }

  val users = new TableWithIdQuery[User, Long, Users](tag => new Users(tag)) {
    /**
     * Extracts the model Id of a arbitrary model.
     * @param model a mapped model
     * @return a Some[I] if Id is filled, None otherwise
     */
    override def extractId(model: User): Option[Long] = model.id

    /**
     *
     * @param model a mapped model (usually without an assigned id).
     * @param id an id, usually generate by the database
     * @return a model M with an assigned id.
     */
    override def withId(model: User, id: Long): User = model.copy(id = Some(id))
  }

  def main(args: Array[String]): Unit = {
    val sys = ActorSystem("system")
    val dbActor = sys actorOf(Props[DbActor], "DbActor")
    import DbActor._
    dbActor ! Entry[User, Long, Users](users, User("pamu nagarjuna"))
  }
}

object DbActor {
  case class Entry[M, I: BaseColumnType, T <: IdTable[M, I]](tableWithIdQuery: TableWithIdQuery[M, I, T], model: M)
}

class DbActor extends Actor {
  import DbActor._
  override def receive = {
    case Entry(tableWithIdQuery, model) => Db.db.withSession { implicit sx =>

      if(MTable.getTables(tableWithIdQuery.baseTableRow.tableName).list.isEmpty)
        tableWithIdQuery.ddl.create

      tableWithIdQuery.save(model)
    }
  }
}

