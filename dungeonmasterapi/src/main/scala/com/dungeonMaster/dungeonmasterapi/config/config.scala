package com.dungeonMaster.dungeonmasterapi

import com.typesafe.config._

object config {
  implicit val config: Config = ConfigFactory.load()
}
