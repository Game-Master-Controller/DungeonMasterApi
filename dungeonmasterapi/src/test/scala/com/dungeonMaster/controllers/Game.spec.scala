package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import _root_.awscala.dynamodbv2.DynamoDB
import awscala._, dynamodbv2._
import scala.concurrent.ExecutionContext
import cats.effect._
import cats.effect.{IO, Async}

class GameTest extends AnyFunSpec with MockFactory {
  describe("Game") {
    describe("create") {
      it("Should create a new game because there is no existing game") {
        assert(true)
      }
    }
  }
}
