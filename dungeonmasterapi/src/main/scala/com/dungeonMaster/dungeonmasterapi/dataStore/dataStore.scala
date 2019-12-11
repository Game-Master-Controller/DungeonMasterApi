package com.dungeonMaster.dungeonmasterapi

import cats.effect.{ExitCode, IO, IOApp}
import awscala._, dynamodbv2._

abstract class DataStore[F[_]] {
  def createEntry(values: Map[String, String]): F[NetworkResponse]
}

abstract class DynamoDataStore[F[_]] extends DataStore[F] {
  val dynamoDbEngine: Option[DynamoDB]
  val table: Option[Table]
  def addTableName(tableName: String): DynamoDataStore[F]
  def addRegion(region: String): DynamoDataStore[F]
}

object DynamoDataStore {
  lazy val dynamoDataStore: DynamoDataStore[IO]  = new DynamoDataStore[IO] {
    val dynamoDbEngine = None
    val table = None
    def addTableName(tableName: String): DynamoDataStore[IO] = ???
    def addRegion(region: String): DynamoDataStore[IO] = ???
    def createEntry(values: Map[String, String]): IO[NetworkResponse] = ???
  }

  implicit def apply: DynamoDataStore[IO] = dynamoDataStore
}
