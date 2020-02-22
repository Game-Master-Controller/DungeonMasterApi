package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import _root_.awscala.dynamodbv2.DynamoDB
import awscala._, dynamodbv2._
import scala.concurrent.ExecutionContext
import cats.effect._
import cats.effect.{IO}
import com.dungeonMaster.dungeonmasterapi.Execs.GameExec
import cats.data.EitherT
import com.dungeonMaster.dungeonmasterapi.TableNames.Games
import com.dungeonMaster.dungeonmasterapi.Execs.GameExec
import cats.effect.IO._

class DungeonGameTest extends AnyFunSpec with MockFactory {

  implicit val ecc = ExecutionContext.global
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  implicit val cs: ContextShift[IO] = IO.contextShift(implicitly[ExecutionContext])

  abstract class ProxyGameProcessor extends GameProcessor[IO]
  abstract class ProxyDataStore extends DataStore[IO]
  abstract class ProxyDynamoFacade extends DynamoDBFacade[IO]

  implicit val mockDynamoFacade = mock[ProxyDynamoFacade]
  implicit val mockDataStore: DataStore[IO] = mock[ProxyDataStore]
  implicit val mockGameProcessor: GameProcessor[IO] = mock[ProxyGameProcessor]
  implicit val mockDynamoDpProxy = mock[DynamoDB]

  describe("DungeonGame") {
    describe("Execs") {
      describe("GameExec") {
        describe("submit") {
          it("Should evaluate to a program that evaluates successful.") {
            val testName = "test game"
            val testGame: Game = Game(testName)

            val testResult = "result"
            val mabyResult : Either[String, String] = Right(testResult)

            val resultingProgram = EitherT(IO(mabyResult))

            (mockGameProcessor.submit (_: Game))
              .expects(testGame)
              .returning(resultingProgram)
  
            val result = testGame.submitGame.value.unsafeRunSync().right.get

            assert(result == testResult)
          }
        }
      }
    }
    describe("IOProcessors") {
      describe("Dnd5eProcessor") {
        val nameOfGame = "game name"
        val dndGame = Game(nameOfGame)
        val value = List(("key", "value"))
        val testResult = "result"
        val mabyResult : Either[String, String] = Right(testResult)

        val resultingProgram = EitherT(IO(mabyResult))

        describe("submit") {
          it("Should evaluate to a program that is successful.") {

            (mockDataStore.createEntry (_:String, _:Option[Seq[(String, Any)]]))
              .expects(dndGame.name, None)
              .returning(resultingProgram)

            import cats.effect.Async

            val gameProcessor = GameProcessor.apply[IO]
            val result = gameProcessor.submit(dndGame).value.unsafeRunSync().right.get

            assert(result == testResult)
          }
        }
      }
    }
    describe("GameController") {
      describe("submitGame") {
        it("Should evaluate to a program that is successful.") {
          val nameOfGame = "coolGameName"

          val expectedGame = Game(nameOfGame)

          import com.dungeonMaster.dungeonmasterapi.Execs.GameExec

          val expectedMessage = "Game Was Successfully Created"

          val mabyResult: Either[String, String] = Right(expectedMessage)

          val resultingProgram = EitherT(IO(mabyResult))

          (mockGameProcessor.submit (_:Game))
            .expects(expectedGame)
            .returning(resultingProgram)

          val expectedResultMessage = ResponseMessage(expectedMessage, None)

          val controller = GameController.apply[IO]

          val actualResult = controller.submitGame(nameOfGame).unsafeRunSync()

          assert(actualResult == expectedResultMessage)
        }
      }
    }
  }
}
