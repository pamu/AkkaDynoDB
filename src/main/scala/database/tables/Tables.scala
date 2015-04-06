package database.tables

import database.columns.{VersionColumn, IdColumn}
import models.{Versionable, Identifiable}

import scala.slick.driver.MySQLDriver.simple._

/**
 * Created by xmax on 7/4/15.
 */

abstract class IdTable[M, I](tag: Tag, schemaName: Option[String], tableName: String)(implicit val colType: BaseColumnType[I])
  extends Table[M](tag, schemaName, tableName) with IdColumn[I] {
  def this(tag: Tag, tableName: String)(implicit mapping: BaseColumnType[I]) = this(tag, None, tableName)
}

abstract class IdVersionTable[M, I](tag: Tag, schemaName: Option[String], tableName: String)(override implicit val colType: BaseColumnType[I])
  extends IdTable[M, I](tag, schemaName, tableName)(colType) with VersionColumn {
  def this(tag: Tag, tableName: String)(implicit  mapping: BaseColumnType[I]) = this(tag, None, tableName)
}

object Tables {
  type EntityTable[M <: Identifiable[M]] = IdTable[M, M#Id]
  type VersionableEntityTable[M <: Identifiable[M] with Versionable[M]] = IdVersionTable[M, M#Id]
}


