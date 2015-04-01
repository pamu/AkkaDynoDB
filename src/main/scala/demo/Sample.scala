package demo

import akka.actor.Actor
import models.{Versionable, Identifiable}

import scala.slick.driver.MySQLDriver.simple._
/**
 * Created by xmax on 1/4/15.
 */
trait IdColumn[I] {
  def id: Column[I]
}

trait VersionColumn {
  def version: Column[Long]
}

abstract class IdTable[M, I](tag: Tag, schemaName: Option[String], tableName: String)(implicit val colType: BaseColumnType[I])
  extends Table[M](tag, schemaName, tableName) with IdColumn[I] {
  def this(tag: Tag, tableName: String)(implicit mapping: BaseColumnType[I]) = this(tag, None, tableName)
}

abstract class IdVersionTable[M, I](tag: Tag, schemaName: Option[String], tableName: String)(override implicit val colType: BaseColumnType)
  extends IdTable[M, I](tag, schemaName, tableName)(colType) with VersionColumn {
  def this(tag: Tag, tableName: String)(implicit  mapping: BaseColumnType[I]) = this(tag, None, tableName)
}

object Utils {
  type EntityTable[M <: Identifiable[M]] = IdTable[M, M#Id]
  type VersionableEntityTable[M <: Identifiable[M] with Versionable[M]] = IdVersionTable[M, M#Id]
}

object Sample {

}

class Sample extends Actor {
  def receive = {
    case "" =>
  }
}
