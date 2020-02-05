package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import _root_.awscala.dynamodbv2.DynamoDB
import awscala._, dynamodbv2._
import scala.concurrent.ExecutionContext
import cats.effect._
import cats.effect.{IO, Async}

class DynamoDataStoreTest extends AnyFunSpec with MockFactory {
  implicit val ecc = ExecutionContext.global
  implicit val timer: Timer[IO] = IO.timer(ExecutionContext.global)
  implicit val cs: ContextShift[IO] = IO.contextShift(implicitly[ExecutionContext])

  val testDynamoDataFacade: DynamoDBFacade[IO] = DynamoDataStoreConfig.apply[IO]
  describe("DynamoDataStore") {
    describe("apply") {
      it("should have a base DynamoDataStore") {
        assert(testDynamoDataFacade.dynamoTable == None)
        assert(testDynamoDataFacade.region == None)
      }
    }
    describe("addRegion") {
      it("Should create a DynamoDataStore with a region given") {
        val testRegion:Option[AWSRegion] = Some(implicitly[USEAST1])
        val dynamoDataStoreWithRegion = testDynamoDataFacade.addRegion(testRegion)
        assert(dynamoDataStoreWithRegion.region == testRegion)
      }
    }
    describe("addTableName") {
      it("Should create a DynamoDataStore with a table name given") {
        val tableName = Some("testTableName")
        val dynamoDataStoreWithTableName = testDynamoDataFacade.addTableName(tableName)
        assert(dynamoDataStoreWithTableName.tableName == tableName)
      }
    }
    describe("connect") {
      describe("Successful connection") {
        it("Should connect given a valid region and table name") {
          val validRegion = implicitly[USEAST1]
          val testRegion: Option[AWSRegion] = Some(validRegion)
          val randomTableName = "testTableName"
          val tableName = Some(randomTableName)
          val testDynamoDataFacadeWithConfig = testDynamoDataFacade.addRegion(testRegion).addTableName(tableName)

          implicit val mockDynamo = mock[DynamoDB]
          val mockConfiguredTable = mock[DynamoDB]
          val mockTable = mock[Table]

          (mockConfiguredTable.table _)
          .expects(randomTableName)
          .returning(Some(mockTable))

          (mockDynamo.at _)
          .expects(validRegion.region)
          .returning(mockConfiguredTable)

          val dynamoDataStore = testDynamoDataFacadeWithConfig.connect

          val connected = dynamoDataStore.value.unsafeRunSync()
          assert(connected.isInstanceOf[Right[_,_]])
        }
      }
      describe("Error when connecting") {
        it("Should error when connecting when region and tablename is not specified") {
          implicit val mockDynamo = mock[DynamoDB]
          val dynamoDataStore = testDynamoDataFacade.connect
          val connected = dynamoDataStore.value.unsafeRunSync()
          assert(connected.left.get == "Neither the tablename or region is specificed")
        }

        it("Should error when connecting when tablename is not specified but the region is") {
          implicit val mockDynamo = mock[DynamoDB]

          val validRegion = implicitly[USEAST1]
          val testRegion: Option[AWSRegion] = Some(validRegion)

          val testDynamoDataFacadeWithRegion = testDynamoDataFacade.addRegion(testRegion)
          val connected = testDynamoDataFacadeWithRegion.connect.value.unsafeRunSync()

          assert(connected.left.get == "The tablename is not specified")
        }

        it("Should error when connecting when the region is not specified but the tablename is") {
          implicit val mockDynamo = mock[DynamoDB]

          val randomTableName = "testTableName"
          val tableName = Some(randomTableName)
          
          val testDynamoDataFacadeWithTable = testDynamoDataFacade.addTableName(tableName)
          val connected = testDynamoDataFacadeWithTable.connect.value.unsafeRunSync()

          assert(connected.left.get == "The region is not specififed")
        }

        it("Should error when connecting to a table that doesnt exist") {
          val validRegion = implicitly[USEAST1]
          val testRegion: Option[AWSRegion] = Some(validRegion)
          val randomWrongTableName = "tableThatDoesntExist"
          val tableName = Some(randomWrongTableName)
          val testDynamoDataFacadeWithConfig = testDynamoDataFacade.addRegion(testRegion).addTableName(tableName)

          implicit val mockDynamo = mock[DynamoDB]
          val mockConfiguredTable = mock[DynamoDB]
          val mockTable = mock[Table]

          (mockConfiguredTable.table _)
          .expects(randomWrongTableName)
          .returning(None)

          (mockDynamo.at _)
          .expects(validRegion.region)
          .returning(mockConfiguredTable)

          val dynamoDataStore = testDynamoDataFacadeWithConfig.connect

          val connected = dynamoDataStore.value.unsafeRunSync()
          assert(connected.left.get == "Table Doesn't Exist")
        }
      }
    }
    describe("createEntry") {
      describe("DynamoDB proxy connected successfully") {
        it("Should add an entry to the data storage and return a successful response") {
          implicit val mockDynamo = mock[DynamoDB]
          val mockConfiguredTable = mock[DynamoDB]
          val validRegion = implicitly[USEAST1]
          val testRegion: Option[AWSRegion] = Some(validRegion)
          val randomTableName = "testTableName"
          val tableName = Some(randomTableName)
          val index = "indexName"
          val testEntry = "index" -> "value"
          val testDynamoDataFacadeWithConfig = testDynamoDataFacade.addRegion(testRegion).addTableName(tableName)

          val mockTable = stub[Table]

          (mockConfiguredTable.table _)
            .expects(randomTableName)
            .returning(Some(mockTable))

          (mockDynamo.at _)
            .expects(validRegion.region)
            .returning(mockConfiguredTable)

          val dynamoDataStore = testDynamoDataFacadeWithConfig.connect

          val connected = dynamoDataStore.value.unsafeRunSync()
          val connectedDynamoDataStore = connected.right.get          

          val response = connectedDynamoDataStore.submitEntry(index, Some(List("atb" -> "asdf"))).value.unsafeRunSync()
          assert(response.right.get == s"Successful entry of $index")
        }

        it("Should return an error after trying to add an entry without configuring region or tablename") {
          implicit val mockDynamo = mock[DynamoDB]
          val mockConfiguredTable = mock[DynamoDB]
          val validRegion = implicitly[USEAST1]
          val testRegion: Option[AWSRegion] = Some(validRegion)
          val randomTableName = "testTableName"
          val tableName = Some(randomTableName)
          val index = "indexName"
          val testEntry = "index" -> "value"
          val testDynamoDataFacadeWithout = testDynamoDataFacade

          val dynamoDataStore = testDynamoDataFacadeWithout.submitEntry(index, Some(List("atb" -> "asdf"))).value.unsafeRunSync()

          assert(dynamoDataStore.left.get == "Table not connected")
        }
      }
    }
  }
}
