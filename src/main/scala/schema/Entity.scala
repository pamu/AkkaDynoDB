package schema

/**
 * Created by pnagarjuna on 21/05/15.
 */
class Entity(json: String, id: String) {
  def _id = id.toLong
  def _json = json
  def copy(id: Long) = new Entity(json, id.toString)
}

object Entity {
  def tupled = {pair: ((String, Long)) => new Entity(pair._1, pair._2.toString)}
  def unapply = {entity: Entity => Some((entity._json, entity._id))}
}
