package com.dungeonMaster.dungeonmasterapi

import cats.data.EitherT
import cats.effect._
import com.dungeonMaster.dungeonmasterapi.TableNames.Games
import cats.Monad
import cats.Applicative
import cats.Functor
import cats.effect.{ConcurrentEffect, ContextShift, Timer}

abstract class GameProcessor[F[_] :Async, D1] {
  def submit(game: Game)(implicit depen1: D1): EitherT[F,String, String]
}

object IOProcessors {
  implicit object IOGameProcessor extends GameProcessor[IO, DataStore[IO, DynamoDBFacade[IO]]] {
    def submit(game: Game)(implicit ds: DataStore[IO, DynamoDBFacade[IO]]): EitherT[IO,String, String] = {
      import com.dungeonMaster.dungeonmasterapi
      ds.createEntry(game.name, None)
    }
  }
}

import IOProcessors._

object Execs {
  implicit class GameExec[F[_]: Async, A](underlying: Game) {
    import com.dungeonMaster.dungeonmasterapi.Main._
    def submitGame(implicit processors: GameProcessor[A], depen: A): EitherT[F, String, String] = processors.submit(underlying)
  }
}

abstract class GameController[C, D, D1] {
  def submitGame[F[_]: ConcurrentEffect](gameName: String)(implicit gameProcessor: GameProcessor[D1]): F[D]
}

case class ResponseMessage(msg: String, error: Option[String] = None)

object IOGameControllers {
  implicit def gameController[F[_]: ConcurrentEffect, Functor] = new GameController[Game, ResponseMessage, DataStore[DynamoDBFacade[F], Games.type]] {
    def submitGame[B](gameName: String)
      (
        implicit gameProcessor: GameProcessor[DataStore[DynamoDBFacade[F],Games.type]],
        depen: DynamoDBFacade[F],
        ds: DataStore[DynamoDBFacade[F], Games.type])
        : F[ResponseMessage] = {
      import com.dungeonMaster.dungeonmasterapi.Execs.GameExec
      import com.dungeonMaster.dungeonmasterapi.DataStores.DynamoDataStore

      gameProcessor.submit(Game(gameName)).fold[ResponseMessage](
        (error) => ResponseMessage("There Was An Error While Creating a Game", Some(error)),
        (msg) => ResponseMessage(msg, None)
      )
    }
  }
  
}








