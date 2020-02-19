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

class DungeonGameTest extends AnyFunSpec with MockFactory {

  case class TestGame(name: String)

  case class Depen1()
  implicit object Depen1 extends Depen1

  implicit val ecc = ExecutionContext.global
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  implicit val cs: ContextShift[IO] = IO.contextShift(implicitly[ExecutionContext])

  abstract class ProxyGameProcessor extends GameProcessor[IO, TestGame, Depen1]
  abstract class ProxyRealGameProcessor extends GameProcessor[IO, Game, Depen1]
  abstract class ProxyDataStore extends DataStore[IO, DynamoDBFacade[IO], Games.type]
  abstract class ProxyDynamoFacade extends DynamoDBFacade[IO]

  val mockDynamoFacade = mock[ProxyDynamoFacade]

  implicit val mockDataStore = mock[ProxyDataStore]

  implicit val mockGameProcessor: GameProcessor[IO, TestGame, Depen1] = mock[ProxyGameProcessor]

  implicit val mockGameProcessorForGame: GameProcessor[IO, Game, Depen1] = mock[ProxyRealGameProcessor]

  
  implicit val mockDynamoDpProxy = mock[DynamoDB]

  describe("DungeonGame") {
    describe("Execs") {
      describe("GameExec") {
        describe("submit") {
          it("Should evaluate to a program that evaluates successful.") {
            val testName = "test game"
            val testGame = TestGame(testName)

            val testResult = "result"
            val mabyResult : Either[String, String] = Right(testResult)

            val resultingProgram = EitherT(IO(mabyResult))

            (mockGameProcessor.submit (_: TestGame)(_: Depen1))
              .expects(testGame, Depen1)
              .returning(resultingProgram)

            val result = testGame.submitGame[IO].value.unsafeRunSync().right.get

            assert(result == testResult)
          }
        }
      }
    }
    describe("IOProcessors") {
      describe("Dnd5eProcessor") {
        import com.dungeonMaster.dungeonmasterapi.IOProcessors.GameProcessor
        val nameOfGame = "game name"
        val dndGame = Game(nameOfGame)
        val value = List(("key", "value"))
        val testResult = "result"
        val mabyResult : Either[String, String] = Right(testResult)

        val resultingProgram = EitherT(IO(mabyResult))

        describe("submit") {
          it("Should evaluate to a program that is successful.") {

            (mockDataStore.createEntry (_:String, _:Option[Seq[(String, Any)]])(_: DynamoDBFacade[IO]))
              .expects(dndGame.name, None, *)
              .returning(resultingProgram)

            val result = GameProcessor.submit(dndGame).value.unsafeRunSync().right.get

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

          val result = "result"

          val mabyResult: Either[String, String] = Right(result)

          val resultingProgram = EitherT(IO(mabyResult))

          (mockGameProcessorForGame.submit (_:Game)(_: Depen1))
            .expects(expectedGame, Depen1)
            .returning(resultingProgram)

          import com.dungeonMaster.dungeonmasterapi.Controllers.IOGameController

          val expectedMessage = "Game Was Successfully Created"

          val expectedResultMessage = ResponseMessage(expectedMessage, None)

          
          val actualResult = IOGameController.submitGame(nameOfGame).unsafeRunSync()

          assert(actualResult == expectedResultMessage)
        }
      }
    }
  }
}
