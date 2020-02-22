package com.dungeonMaster.dungeonmasterapi

import cats.effect.Sync
import cats.implicits._
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl
import cats.data.EitherT
import cats.effect.IO
import com.dungeonMaster.dungeonmasterapi.TableNames.Games
import cats.effect.Async
import com.dungeonMaster.dungeonmasterapi.Encoders._
import cats.Applicative
import io.circe.Encoder
import org.http4s.EntityEncoder
import io.circe.Encoder
import io.circe.{Encoder, Decoder, Json, HCursor}
import org.http4s.circe._
import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Decoder, Json, HCursor}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder, Method, Uri, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.circe._
import com.dungeonMaster.dungeonmasterapi.Jokes.Joke
import com.dungeonMaster.dungeonmasterapi.DataStore._
import cats.effect.ConcurrentEffect
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import cats.Functor
import com.dungeonMaster.dungeonmasterapi.GameProcessor._

object DungeonmasterapiRoutes {

  def gameRoutes[F[_]:Async:ConcurrentEffect](implicit gc: GameController[F, ResponseMessage]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._

    import com.dungeonMaster.dungeonmasterapi.GameController
    import com.dungeonMaster.dungeonmasterapi.GameController._

    HttpRoutes.of[F] {
      case POST -> Root / "game" / nameOfGame =>
        for {
          respMessage <- gc.submitGame(nameOfGame)
          resp <- Ok(respMessage)
        } yield resp
    }
  }
}
