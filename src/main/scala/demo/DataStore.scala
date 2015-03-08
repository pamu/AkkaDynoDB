package demo

import akka.actor.Actor
import akka.routing.ConsistentHashingRouter.ConsistentHashable

/**
 * Created by android on 4/3/15.
 */
object DataStore {
  final case class Evict(key: String)
  
  final case class Get(key: String) extends ConsistentHashable {
    override def consistentHashKey: Any = key
  }
  
  final case class Entry(key: String, value: String)
}

class DataStore extends Actor {
  
  var store = Map.empty[String, String]
  
  override def receive = {
    case Entry(key, value) => store += (key -> value)
    case Get(key) => sender() ! store.get(key)
    case Evict(key) => store -= key
  }
}
