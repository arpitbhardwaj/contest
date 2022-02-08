package com.ab
package faulttolerance

/**
 * When a actor fails, it
 *  suspend its children
 *  send a special message to its children
 *
 * The Parent can decide
 *  resume the actor
 *  restart the actor(default)
 *  stop the actor
 *  escalate and fail itself
 */
object SupervisionDemo extends App {

}
