package com.dungeonMaster.dungeonmasterapi

import org.scalatest.FunSpec
import org.scalacheck._
import org.scalamock.scalatest.MockFactory
import cats.effect.{ExitCode, IO, IOApp}
import org.scalatest.funspec.AnyFunSpec

class DynamoDataStoreTest extends AnyFunSpec with MockFactory {
  val testDynamoDataStore: DynamoDataStore[IO] = implicitly[DynamoDataStore[IO]]
  describe("DynamoDataStore") {
    describe("addRegion") {
      it("should have a base DynamoDataStore") {
        assert(testDynamoDataStore.dynamoDbEngine == None)
        assert(testDynamoDataStore.table == None)
      }
    }
  }
}
