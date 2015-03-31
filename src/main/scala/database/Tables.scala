package database

import models.{ Versionable, Identifiable }

/**
 * Defines Slick table extensions.
 * To be mixed-in into a cake.
 */
trait Tables { this: Profile =>

  import jdbcDriver.simple._

  trait IdColumn[I] {
    def id: Column[I]
  }

  trait VersionColumn {
    def version: Column[Long]
  }

  /** Table extension to be used with a Model that has an Id. */
  abstract class IdTable[M, I](tag: Tag, schemaName: Option[String], tableName: String)(implicit val colType: BaseColumnType[I])
    extends Table[M](tag, schemaName, tableName) with IdColumn[I] {

    /** Constructor without schemaName */
    def this(tag: Tag, tableName: String)(implicit mapping: BaseColumnType[I]) = this(tag, None, tableName)
  }

  /** Table extension to be used with a Model that has an Id and version (optimistic locking). */
  abstract class IdVersionTable[M, I](tag: Tag, schemaName: Option[String], tableName: String)(override implicit val colType: BaseColumnType[I])
    extends IdTable[M, I](tag, schemaName, tableName)(colType) with VersionColumn {

    def this(tag: Tag, tableName: String)(implicit mapping: BaseColumnType[I]) = this(tag, None, tableName)
  }

  /**
   * Type alias for [[IdTable]]s mapping [[models.Identifiable]]s
   * Id type is mapped via type projection of Identifiable#Id
   */
  type EntityTable[M <: Identifiable[M]] = IdTable[M, M#Id]

  /**
   * Type alias for [[IdTable]]s mapping [[models.Identifiable]]s with version.
   * Id type is mapped via type projection of Identifiable#Id
   */
  type VersionableEntityTable[M <: Identifiable[M] with Versionable[M]] = IdVersionTable[M, M#Id]

}
