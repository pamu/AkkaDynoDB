package models

/**
 * Base trait to define a model having an ID (i.e.: Entity).
 * The ID is defined as a type alias as it needs to
 * be accessed by ActiveSlick via type projection when mapping to databse tables.
 */
trait Identifiable[E <: Identifiable[E]] {

  /** The type of this Entity ID */
  type Id

  /**
   * The Entity ID wrapped in an Option.
   * Expected to be None when Entity not yet persisted, otherwise Some[Id]
   */
  def id: Option[E#Id]

  /**
   * Provide the means to assign an ID to the entity
   * @return A copy of this Entity with an ID.
   */
  def withId(id: E#Id): E
}