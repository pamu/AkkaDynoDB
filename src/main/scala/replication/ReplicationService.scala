package replication

import akka.actor.Actor

/**
 * Created by android on 14/3/15.
 */
object ReplicationService {
  case object Replicate
}

class ReplicationService extends Actor {

  import ReplicationService._

  def receive = {
    case Replicate => {

    }
  }
}
