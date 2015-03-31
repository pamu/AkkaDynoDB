package database.exceptions

import models.Versionable

case class StaleObjectStateException[T <: Versionable[T]](staleObject: T, current: T)
  extends ActiveSlickException(s"Optimistic locking error - object in stale state: $staleObject, current in DB: $current")