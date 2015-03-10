package demo

import akka.actor.Actor
import akka.routing.ConsistentHashingRouter.ConsistentHashable

/**
 * Created by android on 4/3/15.
 */
/** DataStore Companion object for the DataStore Messages */
object DataStore {
  //Evict message removes the entry with given key
  final case class Evict(key: String)

  //Gets the value of the corresponding key
  final case class Get(key: String) extends ConsistentHashable {
    override def consistentHashKey: Any = key
  }

  //Adds the entry to the key value store
  final case class Entry(key: String, value: String)
}

/** DataStore Actor acts in the database of key value pairs */
class DataStore extends Actor {
  //import the messages from the object
  import DataStore._

  //immutable Map data structure assigned to mutable variable
  var store = Map.empty[String, String]
  
  override def receive = {

    case Entry(key, value) => store += (key -> value)

    case Get(key) => sender() ! store.get(key)

    case Evict(key) => store -= key

  }
}
