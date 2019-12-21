package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._
import org.scalatest.FunSpec
import org.scalacheck._
import org.scalamock.scalatest.MockFactory
import cats.effect.{ExitCode, IO, IOApp, Async}
import org.scalatest.funspec.AnyFunSpec
import _root_.awscala.dynamodbv2.DynamoDB
import awscala._, dynamodbv2._
import cats.implicits._
import cats.data.EitherT
import cats.effect._
import cats.syntax.all._
import scala.concurrent.duration._
import cats._
import scala.concurrent.ExecutionContext

class DynamoDataStoreTest extends AnyFunSpec with MockFactory {
  implicit val ecc = ExecutionContext.global
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  implicit val cs: ContextShift[IO] = IO.contextShift(implicitly[ExecutionContext])

  val testDynamoDataStore: DynamoDataStore[IO] = implicitly[DynamoDataStore[IO]]
  describe("DynamoDataStore") {
    describe("apply") {
      it("should have a base DynamoDataStore") {
        assert(testDynamoDataStore.dynamoTable == None)
        assert(testDynamoDataStore.region == None)
    }
    }
    describe("addRegion") {
      it("Should create a DynamoDataStore with a region given") {
        val testRegion:Option[AWSRegion] = Some(implicitly[USEAST1])
        val dynamoDataStoreWithRegion = testDynamoDataStore.addRegion(testRegion)
        assert(dynamoDataStoreWithRegion.region == testRegion)
      }
    }
    describe("addTableName") {
      it("Should create a DynamoDataStore with a table name given") {
        val tableName = Some("testTableName")
        val dynamoDataStoreWithTableName = testDynamoDataStore.addTableName(tableName)
        assert(dynamoDataStoreWithTableName.tableName == tableName)
      }
    }
    describe("Connect") {
      it("Should connect given a valid region and table name") {
        val validRegion = implicitly[USEAST1]
        val testRegion: Option[AWSRegion] = Some(validRegion)
        val randomTableName = "testTableName"
        val tableName = Some(randomTableName)
        val testDynamoDataStoreWithConfig = testDynamoDataStore.addRegion(testRegion).addTableName(tableName)

        val mockDynamo = mock[DynamoDB]
        val mockConfiguredTable = mock[DynamoDB]
        val mockTable = mock[Table]

        (mockConfiguredTable.table _)
          .expects(randomTableName)
          .returning(Some(mockTable))

        (mockDynamo.at _)
          .expects(validRegion.region)
          .returning(mockConfiguredTable)

        val dynamoDataStore = testDynamoDataStoreWithConfig.connect[IO](dynamoDbProxy = mockDynamo, ec = ecc,timer = timer, cf = cs)
        // , timer = timer, cf = cs

        val connected = dynamoDataStore.value.unsafeRunSync()
        assert(connected.isInstanceOf[Right[_,_]])
      }
    }
  }
}
