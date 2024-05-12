package com.ab.clustering;

/**
 * Build distributed applications
 *  decentralized peer to peer
 *  no single point of failure
 *  automatic node membership and gossip protocol
 *  failure detector
 *
 *  Clustering is based on remoting
 *    in most cases use clustering instead of remoting
 *
 * Clusters
 *  composed of member nodes
 *    node = host + port + UID
 *    on the same jvm
 *    on multiple jvms on the same machine
 *    on a set of machine of any scale
 *
 * Cluster membership
 *  convergent gossip protocol
 *  phi accrual failure detector - same as remoting
 *  no leader election - leader is deterministically chosen
 *
 * Join a cluster
 *  contact seed nodes in order (from config)
 *    if i am the first seed node, i will join myself
 *    send a join command to the seed node that responds first
 *  node is in the joining state
 *    wait for gossip to converge
 *    all nodes in the cluster must acknowledge the new node
 *  the leader will set the state of new node to up
 *
 * Leave a cluster
 *  Option 1: Safe and quite
 *    node switches its state to leaving
 *    gossip converges
 *    leaders set the state to "existing"
 *    gossip converges
 *    leaders marks it removed
 *  Option 2: The hard way
 *    a node becomes unreachable
 *    gossip convergence and leader actions are not possible
 *    must be removed (download) manually
 *    cal also be auto downed bt the leader
 *    DO NOT USE auto downing in prod
 */