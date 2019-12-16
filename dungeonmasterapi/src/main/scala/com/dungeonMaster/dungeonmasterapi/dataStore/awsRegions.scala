package com.dungeonMaster.dungeonmasterapi

object AWSRegions extends Enumeration {
  type AWSRegions = Value
  protected case class RegionValue(regionName: String) extends super.Val
  val USEAST1 = RegionValue("us-east-1")
}
