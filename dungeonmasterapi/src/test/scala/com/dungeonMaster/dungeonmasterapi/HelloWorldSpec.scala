package com.dungeonMaster.dungeonmasterapi

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class GameRoutesSpec extends org.specs2.mutable.Specification {

  "Game" >> {
    "return success message" >> {
      uriReturnsSuccessfulMessage()
    }
  }

  private[this] val retHelloWorld: Response[IO] = {
    val getHW = Request[IO](Method.GET, uri"/hello/world")
    val helloWorld = HelloWorld.impl[IO]
    DungeonmasterapiRoutes.helloWorldRoutes(helloWorld).orNotFound(getHW).unsafeRunSync()
  }

  private[this] val retGameCreate: Response[IO] = {
    val getHW = Request[IO](Method.POST, uri"/game/gameName")
    DungeonmasterapiRoutes.gameRoutes.orNotFound(getHW).unsafeRunSync()
  }

  private[this] def uriReturns200(): MatchResult[Status] =
    retHelloWorld.status must beEqualTo(Status.Ok)

  private[this] def uriReturnsHelloWorld(): MatchResult[String] =
    retHelloWorld.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")

  private[this] def uriReturnsSuccessfulMessage(): MatchResult[String] =
    retGameCreate.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Successful entry of gameName\"}")

}
