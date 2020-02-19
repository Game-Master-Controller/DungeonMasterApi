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
import com.dungeonMaster.dungeonmasterapi.IOGameControllers._
import com.dungeonMaster.dungeonmasterapi.IOGameControllers._
import com.dungeonMaster.dungeonmasterapi.DataStores._
import com.dungeonMaster.dungeonmasterapi.IOProcessors._
import cats.effect.ConcurrentEffect
import cats.effect.{ConcurrentEffect, ContextShift, Timer}
import com.dungeonMaster.dungeonmasterapi.IOProcessors._
import cats.Functor

object DungeonmasterapiRoutes {

  def gameRoutes[F[_]:Async:ConcurrentEffect:Functor](implicit T: Timer[F], C: ContextShift[F]): HttpRoutes[F] = {
    val dsl = new Http4sDsl[F]{}
    import dsl._
    //val controller = implicitly[GameController[Game, ResponseMessage, DynamoDBFacade[F]]]
    val controller = IOGameControllers.IOGameController

    import com.dungeonMaster.dungeonmasterapi.IOProcessors._

    //val gp: GameProcessor[DataStore[DynamoDBFacade[F], Games.type]] = implicitly

    HttpRoutes.of[F] {
      case POST -> Root / "game" / nameOfGame =>
        for {
          respMessage <- controller.submitGame[F](nameOfGame)
          resp <- Ok(respMessage)
        } yield resp
    }
  }
}
