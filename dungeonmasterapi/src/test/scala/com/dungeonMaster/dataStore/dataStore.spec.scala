package com.dungeonMaster.dungeonmasterapi

import org.scalatest.FunSpec
import org.scalacheck._
import org.scalamock.scalatest.MockFactory
import cats.effect.{ExitCode, IO, IOApp}
import org.scalatest.funspec.AnyFunSpec
import com.dungeonMaster.dungeonmasterapi.AWSRegions._

class DynamoDataStoreTest extends AnyFunSpec with MockFactory {
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
        val testRegion = Some(USEAST1)
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
  }
}
