package com.dungeonMaster.dungeonmasterapi

import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.implicits._
import fs2.Stream
import org.http4s.client.blaze.BlazeClientBuilder
import org.http4s.implicits._
import org.http4s.server.blaze.BlazeServerBuilder
import org.http4s.server.middleware.Logger
import scala.concurrent.ExecutionContext.global
import cats.effect.IO
import scala.concurrent.ExecutionContext
import cats.data.Kleisli
import org.http4s.Request
import org.http4s.Response
import com.dungeonMaster.dungeonmasterapi.GameController._
import cats.effect.Async
import cats.effect.Effect

object DungeonmasterapiServer {

  implicit val par = IO.contextShift(ExecutionContext.global)

  def stream[F[_]: ConcurrentEffect:Async](implicit T: Timer[F], C: ContextShift[F]): Stream[F, Nothing] = {

    for {
      client <- BlazeClientBuilder[F](global).stream
      
      /*
       Combine Service Routes into an HttpApp.
       Can also be done via a Router if you
       want to extract a segments not checked
       in the underlying routes.
       */
      

      httpApp: Kleisli[F ,Request[F],Response[F]] = (
        DungeonmasterapiRoutes.gameRoutes[F]
      ).orNotFound

      /* With Middlewares in place */
      finalHttpApp = Logger.httpApp(true, true)(httpApp)

      exitCode <- BlazeServerBuilder[F]
        .bindHttp(8080, "0.0.0.0")
        .withHttpApp(finalHttpApp)
        .serve
    } yield exitCode
  }.drain
}
