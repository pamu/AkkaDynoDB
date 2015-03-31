package database.exceptions

case object NoRowsAffectedException extends ActiveSlickException("No rows affected")
