package models

import akka.cluster.VectorClock

/**
 * Created by xmax on 30/3/15.
 */
case class VersionedData[T](id: Long, version: VectorClock, data: T)
