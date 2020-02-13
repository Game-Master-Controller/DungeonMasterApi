package com.dungeonMaster.dungeonmasterapi

import cats.data.EitherT
import cats.effect._

abstract class GameProcessor[F[+_], A, B] {
  def submit(game: A)(implicit ds: B): EitherT[F,String, String]
}

object IOProcessors {
  implicit object GameProcessor extends GameProcessor[IO, Game, DataStore[IO, DynamoDBFacade[IO]]] {
    def submit(game: Game)(implicit ds: DataStore[IO, DynamoDBFacade[IO]]): EitherT[IO,String, String] = {
      import com.dungeonMaster.dungeonmasterapi.DynamoDataStoreConfig.apply
      ds.createEntry(game.name, None)
    }
  }
}

object Execs {
  implicit class GameExec[A, B](underlying: A) {
    def submitGame[F[+_]](implicit processors: GameProcessor[F, A, B], dependency: B): EitherT[F, String, String] = processors.submit(underlying)
  }
}
abstract class GameController[F[_], C, D] {
  def submitGame[B](gameName: String)(implicit gameProcessor: GameProcessor[IO, Game, B], depen: B): F[D]
}

case class ResponseMessage(msg: String)

package object Controllers {
  implicit object IOGameController extends GameController[IO, Game, ResponseMessage] {
    def submitGame[B](gameName: String)(implicit gameProcessor: GameProcessor[IO, Game, B], depen: B): IO[ResponseMessage] = {
      import com.dungeonMaster.dungeonmasterapi.Execs.GameExec
      import com.dungeonMaster.dungeonmasterapi.DataStores.DynamoDataStore

      Game(gameName).submitGame[IO].value.map(resp => {
        resp match {
          case Left(error) => ResponseMessage("There Was An Error While Creating a Game")
          case Right(message) => ResponseMessage("Game Was Successfully Created")
        }
      })
      
    }
  }
}








