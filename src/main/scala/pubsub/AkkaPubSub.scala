import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Address, Props}
import akka.cluster.Cluster
import akka.cluster.ClusterEvent.{CurrentClusterState, InitialStateAsEvents, MemberEvent, MemberUp, UnreachableMember}
import akka.cluster.pubsub.DistributedPubSub
import akka.cluster.pubsub.DistributedPubSubMediator.{Publish, Subscribe, SubscribeAck, Unsubscribe}
import com.typesafe.config.ConfigFactory

case class PublishMessage(topic: String, message: String)
case class SubscribeToTopic(topic: String)
case class UnsubscribeFromTopic(topic: String)

// Publisher
class Publisher extends Actor with ActorLogging{
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  def receive: Receive = {
    case PublishMessage(topic, message) =>
      mediator ! Publish(topic, message)
  }
}

// Subscriber
class Subscriber extends Actor with ActorLogging {
  val mediator: ActorRef = DistributedPubSub(context.system).mediator
  val cluster: Cluster = Cluster(context.system)

  override def preStart(): Unit = {
    cluster.subscribe(
      self,
      initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent],
      classOf[UnreachableMember]
    )
  }

  override def postStop(): Unit = {
    cluster.unsubscribe(self)
  }

  def receive: Receive = {
    case SubscribeToTopic(topic) =>
      log.info(s"Subscribing to $topic")
      mediator ! Subscribe(topic, self)
    case UnsubscribeFromTopic(topic) =>
      log.info(s"Unsubscribing from $topic")
      mediator ! Unsubscribe(topic, self)
    case SubscribeAck(Subscribe(topic, _, _)) =>
      log.info(s"Subscribed to $topic")
    case msg: String =>
      log.info(s"Subscriber received: $msg")
    case state: CurrentClusterState =>
      log.info(s"Current members: ${state.members.mkString(", ")}")
    case MemberUp(member) =>
      log.info(s"Member is Up: ${member.address}")
  }
}

object PubSubMain extends App {
  val system = ActorSystem(
    "AbixelCluster",
    ConfigFactory.load("pubsub/akkaPubSub.conf")
  )
  val cluster = Cluster(system)
  val address = Address("akka", "AbixelCluster", "localhost", 2551)
  cluster.join(address)

  val publisher = system.actorOf(Props[Publisher], "publisher")
  val subscriber = system.actorOf(Props[Subscriber], "subscriber")
  subscriber ! SubscribeToTopic("content")

  Thread.sleep(5000) // Let's wait a bit to make sure that the subscriber is ready to receive messages
  publisher ! PublishMessage("content", "Hello, Akka Cluster!")
}
