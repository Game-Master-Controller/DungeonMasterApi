package com.dungeonMaster.dungeonmasterapi

import awscala._, dynamodbv2._
import org.scalamock.scalatest.MockFactory
import org.scalatest.funspec.AnyFunSpec
import _root_.awscala.dynamodbv2.DynamoDB
import awscala._, dynamodbv2._
import scala.concurrent.ExecutionContext
import cats.effect._
import cats.effect.{IO, Async}

class AWSRegionTest extends AnyFunSpec with MockFactory {
  describe("AWSRegion") {
    describe("getRegion") {
      it("Should return a usEast1 region") {
        val usEast1Region = AWSRegion.getRegion("us-east-1")
        assert(usEast1Region.regionName == "us-east-1")
      }

      it("Should return the default region") {
        val defaultRegion = AWSRegion.getRegion("")
        assert(defaultRegion.regionName == "us-east-1")
      }
    }
  }
}
