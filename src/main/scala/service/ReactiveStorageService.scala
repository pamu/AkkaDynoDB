package service

import akka.actor.{Props, ActorSystem, Actor}
import akka.routing.ConsistentHashingRouter.ConsistentHashableEnvelope
import akka.routing.FromConfig
import com.typesafe.config.ConfigFactory
import constants.Constants
import replication.Replicator
import storage.StorageNode

/**
 * Created by android on 10/3/15.
 */


class ReactiveStorageService extends Actor {

  val workerRouter = context.actorOf(FromConfig.props(Props[StorageNode]), name = Constants.Router)
  // import the worker node message get, entry, evict
  import StorageNode._

  // actor receive method
  override def receive = {

    case Get(key) => {
      //Get message from the client
      //Wrap the message in the consistent hashable envelope and send it to the router
      workerRouter forward ConsistentHashableEnvelope(message = Get(key), hashKey = key)
    }
    case Entry(key, value) => {
      //Entry message from the client to add the key to the store
      //wrap the message in the envelope
      workerRouter forward ConsistentHashableEnvelope(message = Entry(key,value), hashKey = key)
    }

    case Evict(key) => {
      //Evict operation message from the client
      //wrap the evict key in the envelope
      workerRouter forward ConsistentHashableEnvelope(message = Evict(key), hashKey = key)
    }

    case All(key) => {
      //All operation message from the client
      //wrap the all key in the envelope
      workerRouter forward ConsistentHashableEnvelope(message = All(key), hashKey = key)
    }
  }
}


object Starter {

  def main(args: Array[String]): Unit = {
    //use the port number given as command line args or use random port
    val port = if(args.isEmpty) "0" else args(0)

    //read the configuration in the file rss.conf
    val config = ConfigFactory.parseString(s"akka.remote.netty.tcp.port=$port")
      .withFallback(ConfigFactory.parseString("akka.cluster.roles = [storage]"))
      .withFallback(ConfigFactory.load("rss"))

    //get the ref of the actor system
    val system = ActorSystem("ClusterSystem", config)

    //start the worker actor which does the real storing stuff
    system.actorOf(Props[StorageNode], name = Constants.StorageNode)

    //starting akka storage service actor
    system.actorOf(Props[ReactiveStorageService], name = Constants.ReactiveStorageService)

    //starting akka replicator service actor
    //system.actorOf(Props[Replicator], Constants.Replicator)
  }
}
