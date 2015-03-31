package database.exceptions

case class RowNotFoundException[T](notFoundRecord: T)
  extends ActiveSlickException(s"Row not found: $notFoundRecord")