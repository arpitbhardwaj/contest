package com.ab.actors;

/**
 *
 * can we assume any ordering of messages?
 * Aren't we causing race conditions?
 * What does asynchronous actually means for an actor
 *
 * Akka has thread pool that it shared with actors
 *
 * Akka Actors is a complex data structure contains
 *      message handler (receive)
 *      message queue
 * At some of time
 *      a thread is scheduled to run this actor
 *      messages are extracted from the mailbox
 *      the thread invokes the handler on each message
 *      at some point the actor is unscheduled
 *
 * Conditions
 *      only one thread operates on an actor at any point of time
 *      means actors are effectively single threaded
 *      no locks needed
 *
 *      Message Delivery Guarantees
 *      at once delivery (no one receive duplicates)
 *      if Alice send Bob messages A followed by B
 *          means bob will never receive duplicates of A and B
 *          will always receive A before B
 */