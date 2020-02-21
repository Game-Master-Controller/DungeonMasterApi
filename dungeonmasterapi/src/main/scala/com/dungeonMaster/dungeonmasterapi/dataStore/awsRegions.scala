package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._

abstract class AWSRegion {
  val regionName: String
  val region: Region
}

abstract class USEAST1 extends AWSRegion
case object USEAST1 extends USEAST1 {
  val regionName: String = "us-east-1"
  val region: Region = Region.US_EAST_1
}
