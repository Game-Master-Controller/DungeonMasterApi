package com.dungeonMaster.dungeonmasterapi

import com.dungeonMaster.dungeonmasterapi.AWSRegions._
import cats.effect.{ExitCode, IO, IOApp}
import awscala._, dynamodbv2._

abstract class DataStore[F[_]] {
  def createEntry(values: Map[String, String]): F[NetworkResponse]
}

abstract class DynamoDataStore[F[_]] extends DataStore[F] {
  val dynamoTable: Option[Table]
  val region: Option[AWSRegions]
  val tableName: Option[String]
  def addTableName(tableName: Option[String]): DynamoDataStore[F]
  def addRegion(region: Option[AWSRegions]): DynamoDataStore[F]
  def connect: F[NetworkResponse]
  def createEntry(values: Map[String, String]): F[NetworkResponse] = ???
}

object DynamoDataStore {
  private def constructor(table: Option[Table] = None, givenRegion: Option[AWSRegions] = None, givenTableName: Option[String] = None): DynamoDataStore[IO] = new DynamoDataStore[IO] {
      val dynamoTable: Option[Table] = table
      val region: Option[AWSRegions] = givenRegion
      val tableName: Option[String] = givenTableName
      def addTableName(tableName: Option[String]): DynamoDataStore[IO] = constructor(table, givenRegion, tableName)
      def addRegion(providedRegion: Option[AWSRegions]): DynamoDataStore[IO] = constructor(table, providedRegion, givenTableName)
      override def createEntry(values: Map[String, String]): IO[NetworkResponse] = ???
      def connect: IO[NetworkResponse] = ???
  }

  lazy val dynamoDataStore: DynamoDataStore[IO] = constructor()

  implicit def apply: DynamoDataStore[IO] = dynamoDataStore
}
