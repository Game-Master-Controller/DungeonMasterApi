package com.dungeonMaster.dungeonmasterapi

import cats.effect.{ExitCode, IO, IOApp}
import cats.implicits._
import cats.effect.{Concurrent, Effect, IO, CancelToken, SyncIO}

object Main extends IOApp {

  object InitialDatabaseSetup {
    implicit val dynamoConnection = DataStores.getConnectedDb[IO, IO].unsafeRunSync
  }

  def run(args: List[String]) =
    DungeonmasterapiServer.stream[IO].compile.drain.as(ExitCode.Success)
}
