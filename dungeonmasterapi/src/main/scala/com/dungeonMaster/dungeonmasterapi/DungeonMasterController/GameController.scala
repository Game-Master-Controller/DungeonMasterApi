package com.dungeonMaster.dungeonmasterapi

import cats.data.EitherT
import cats.effect._
import com.dungeonMaster.dungeonmasterapi.TableNames.Games
import cats.Monad
import cats.Applicative
import cats.Functor
import cats.effect.{ConcurrentEffect, ContextShift, Timer}

abstract class GameProcessor[F[_]:Async] {
  def submit(game: Game): EitherT[F,String, String]
}

object GameProcessor {
  implicit def apply[F[_]: Async](implicit ds: DataStore[F]) = new GameProcessor[F] {
    def submit(game: Game): EitherT[F,String, String] = {
      ds.createEntry(game.name, None)
    }
  }
}

object Execs {
  implicit class GameExec[F[_]: Async](underlying: Game) {
    def submitGame(implicit processors: GameProcessor[F], dep: DataStore[F]): EitherT[F, String, String] = processors.submit(underlying)
  }
}

abstract class GameController[F[_]: Async, D] {
  def submitGame(gameName: String)(implicit gameProcessor: GameProcessor[F]): F[D]
}

case class ResponseMessage(msg: String, error: Option[String] = None)

object GameController {
  implicit def apply[F[_]:Async] = new GameController[F, ResponseMessage] {
    def submitGame(gameName: String)(implicit gameProcessor: GameProcessor[F]): F[ResponseMessage] = {
      import com.dungeonMaster.dungeonmasterapi.Execs.GameExec
      import com.dungeonMaster.dungeonmasterapi.GameProcessor._

      gameProcessor.submit(Game(gameName)).fold[ResponseMessage](
        (error) => ResponseMessage("There Was An Error While Creating a Game", Some(error)),
        (msg) => ResponseMessage(msg, None)
      )
    }
  }
  
}








