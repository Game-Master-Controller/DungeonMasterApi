package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._

abstract class AWSRegion {
  val regionName: String
  val region: Region
}

case class USEAST1(regionName: String = "us-east-1", region: Region = Region.US_EAST_1) extends AWSRegion

object AWSRegion {
  implicit val usEast1 = USEAST1()
}
