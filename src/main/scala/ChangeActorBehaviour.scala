package com.ab

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ChangeActorBehaviour extends App {

  object FussyKid{
    case object KidAccept
    case object KidReject
    val HAPPY = "happy"
    val SAD = "sad"
  }

  //stateful
  class FussyKid extends Actor{
    import FussyKid._
    import Mom._
    var state = HAPPY
    override def receive: Receive = {
      case Food(VEGETABLE) => state = SAD
      case Food(CHOCOLATE) => state = HAPPY
      case Ask(_) =>
        if (state == HAPPY)
          sender() ! KidAccept
        else
          sender() ! KidReject
    }
  }

  //stateless
  class StateLessFussyKid extends Actor{
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      //case Food(VEGETABLE) => context.become(sadReceive)
      //equivalent to where true means discard the old message handler and fully replace it with new msg handler
      //case Food(VEGETABLE) => context.become(sadReceive,true)
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCOLATE) => //stay happy
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => //stay sad
      case Food(CHOCOLATE) => context.become(happyReceive,false)
      case Ask(_) => sender() ! KidReject
    }
  }

  class StateLessFussyKid2 extends Actor{
    import FussyKid._
    import Mom._
    override def receive: Receive = happyReceive
    def happyReceive: Receive = {
      //case Food(VEGETABLE) => context.become(sadReceive)
      //equivalent to where true means discard the old message handler and fully replace it with new msg handler
      //case Food(VEGETABLE) => context.become(sadReceive,true)
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCOLATE) => //stay happy
      case Ask(_) => sender() ! KidAccept
    }
    def sadReceive: Receive = {
      case Food(VEGETABLE) => context.become(sadReceive,false)
      case Food(CHOCOLATE) => context.unbecome()
      case Ask(_) => sender() ! KidReject
    }
  }

  object Mom{
    case class MomStart(kidRef:ActorRef)
    case class Food(food:String)
    case class Ask(msg:String)
    val VEGETABLE = "veggies"
    val CHOCOLATE = "chocos"
  }
  class Mom extends Actor{
    import FussyKid._
    import Mom._
    override def receive: Receive = {
      case MomStart(kidRef) =>
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(VEGETABLE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Food(CHOCOLATE)
        kidRef ! Ask("Do you want to play?")
      case KidAccept => println("Yay, My kid is happy and want to play")
      case KidReject => println("My kid is sad, but as he's healthy")
    }
  }

  import Mom._
  val system = ActorSystem("changeActorBehaviourSystem")
  val mom = system.actorOf(Props[Mom])
  val fussyKid = system.actorOf(Props[FussyKid])
  val statelessFussyKid = system.actorOf(Props[StateLessFussyKid])
  val statelessFussyKid2 = system.actorOf(Props[StateLessFussyKid2])

  /*
  mom receives MomStart
      kid receives Food(Veg) -> kid will change the behaviour to sadReceive
      kid receives Ask(_) -> kid replies with the sadReceive handler
  mom receives kidReject
   */
  mom ! MomStart(fussyKid)

  /*
      kid receives Food(Veg) -> stack.push(sadReceive)
      kid receives Food(choco) -> stack.push(happyReceive)

  Stack:
  3.happyReceive
  2.sadReceive
  1.happyReceive
   */
  mom ! MomStart(statelessFussyKid)

  /*
      kid receives Food(Veg) -> stack.push(sadReceive)
      kid receives Food(Veg) -> stack.push(sadReceive)
      kid receives Food(choco) -> stack.pop(sadReceive)
      kid receives Food(choco) -> stack.pop(sadReceive)
  Stack:
  3.sadReceive (popped)
  2.sadReceive (popped)
  1.happyReceive
   */
  mom ! MomStart(statelessFussyKid2)
}
