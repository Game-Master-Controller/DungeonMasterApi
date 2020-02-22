package com.dungeonMaster.dungeonmasterapi

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.effect.{Concurrent, Effect, IO, CancelToken, SyncIO}

object Main extends IOApp {

  def run(args: List[String]) =
    DungeonmasterapiServer.stream[IO].compile.drain.as(ExitCode.Success)
}
