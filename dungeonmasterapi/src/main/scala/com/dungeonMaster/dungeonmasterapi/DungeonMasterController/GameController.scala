package com.dungeonMaster.dungeonmasterapi

import cats.data.EitherT
import cats.effect._
import com.dungeonMaster.dungeonmasterapi.TableNames.Games
import cats.Monad
import cats.Applicative
import cats.Functor

abstract class GameProcessor[D1] {
  def submit[F[_]: ConcurrentEffect](game: Game)(implicit depen1: D1): EitherT[F,String, String]
}

object IOProcessors {
  implicit def gameProcessor[F[_]: ConcurrentEffect] = new GameProcessor[DataStore[DynamoDBFacade[F], Games.type]] {
    def submit(game: Game)(implicit ds: DataStore[DynamoDBFacade[F], Games.type], db: DynamoDBFacade[F]): EitherT[F,String, String] = {
      ds.createEntry[F](game.name, None)
    }
  }
}

import IOProcessors._

object Execs {
  implicit class GameExec[A](underlying: Game) {
    import com.dungeonMaster.dungeonmasterapi.Main._
    def submitGame[F[_]: ConcurrentEffect](implicit processors: GameProcessor[A], depen: A): EitherT[F, String, String] = processors.submit(underlying)
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








