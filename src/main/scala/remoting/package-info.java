package com.ab.remoting;

/**
 *
 * Actor Model Principles
 *  every interaction based on sending messages
 *  full actor encapsulation
 *  no locking
 *  message sending latency
 *  at most once message delivery
 *  message ordering maintained per sender/receive pair
 *
 * The Principles holds
 *  on the same JVM in parallel application
 *  locally on multiple JVMs
 *  in a distributed env on any scale
 *
 *
 * Location Transparency -  the real actor can be anywhere
 * Akka remoting is based on location transparency
 * We communicate with actors via the reference
 *
 * Transparent Remoting - we are using the object as it were local but bts it communicates remotely
 * JAVA RMI uses transparent remoting
 *
 * #artery is akka remoting implementation
 */