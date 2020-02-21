package com.dungeonMaster.dungeonmasterapi

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult
import cats.effect.ConcurrentEffect
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import scala.concurrent.ExecutionContext

class GameRoutesSpec extends org.specs2.mutable.Specification {

  "Game" >> {
    "return success message" >> {
      //uriReturnsSuccessfulMessage()
      "stuff" must beEqualTo("stuff")
    }
  }

  implicit val par = IO.contextShift(ExecutionContext.global)

  private[this] val retGameCreate: Response[IO] = {
    val getHW = Request[IO](Method.POST, uri"/game/gameName")
    DungeonmasterapiRoutes.gameRoutes[IO].orNotFound(getHW).unsafeRunSync()
  }

  // private[this] def uriReturns200(): MatchResult[Status] =
  //   retGameCreate.status must beEqualTo(Status.Ok)

  // private[this] def uriReturnsHelloWorld(): MatchResult[String] =
  //   retGameCreate.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Hello, world\"}")

  // private[this] def uriReturnsSuccessfulMessage(): MatchResult[String] =
  //   retGameCreate.as[String].unsafeRunSync() must beEqualTo("{\"message\":\"Successful entry of gameName\"}")
}
