package client

import akka.actor.{ActorLogging, Actor}

/**
 * Created by android on 10/3/15.
 */
object AkkaStorageServiceClient {
  case object Start
}

class AkkaStorageServiceClient extends Actor with ActorLogging {
  import AkkaStorageServiceClient._
  override def receive = {
    case Start => log.info("Started Ass Client")
  }
}

object Starter {
  def main(args: Array[String]): Unit = {
    //start the ass client
  }
}