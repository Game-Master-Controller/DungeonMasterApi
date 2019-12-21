package com.dungeonMaster.dungeonmasterapi

import cats.effect._
import awscala._, dynamodbv2._
import cats.Monad
import cats.data.EitherT
import scala.language.higherKinds
import scala.concurrent.ExecutionContext
import cats.syntax.all._
import scala.concurrent.duration._
import cats.implicits._
import scala.concurrent.ExecutionContext._

abstract class DataStore[F[_]] {
  def createEntry(values: Map[String, String]): F[NetworkResponse]
}

abstract class DynamoDataStore[F[_]] extends DataStore[F] {
  val dynamoTable: Option[Table]
  val region: Option[AWSRegion]
  val tableName: Option[String]
  def addTableName(tableName: Option[String]): DynamoDataStore[F]
  def addRegion(region: Option[AWSRegion]): DynamoDataStore[F]
  def connect[F[+_]](implicit dynamoDbProxy: DynamoDB, ec: ExecutionContext, timer: Timer[F], cf: ContextShift[F]): EitherT[IO, String, DynamoDataStore[F]]
  def createEntry(values: Map[String, String]): F[NetworkResponse] = ???
}

object DynamoDataStore {

  private def constructor[F[+_]](table: Option[Table] = None, givenRegion: Option[AWSRegion] = None, givenTableName: Option[String] = None): DynamoDataStore[F] = new DynamoDataStore[F] {
      val dynamoTable: Option[Table] = table
      val region: Option[AWSRegion] = givenRegion
      val tableName: Option[String] = givenTableName
      def addTableName(tableName: Option[String]): DynamoDataStore[F] = constructor[F](table, givenRegion, tableName)
      def addRegion(providedRegion: Option[AWSRegion]): DynamoDataStore[F] = constructor[F](table, providedRegion, givenTableName)
      override def createEntry(values: Map[String, String]): F[NetworkResponse] = ???
      def connect[F[+_]](implicit dynamoDbProxy: DynamoDB, ec: ExecutionContext, timer: Timer[F], cf: ContextShift[F]): EitherT[IO, String, DynamoDataStore[F]] = {
        val config: Either[String ,Tuple2[AWSRegion, String]] = mapConfigToEither(region, tableName)
        val mabyTable: EitherT[IO, String, Table] = querryTable[F](config, dynamoDbProxy)
        for {
          tableOrError <- mabyTable
          newDynamoStore = constructor[F](Some(tableOrError), givenRegion, givenTableName)
        } yield newDynamoStore
      }
  }
  implicit def apply[F[+_]]: DynamoDataStore[F] = constructor()

  private def querryTable[F[_]](config: Either[String ,Tuple2[AWSRegion, String]], dynamoDbProxy: DynamoDB)(implicit ec: ExecutionContext, timer: Timer[F], cf: ContextShift[F]): EitherT[IO, String, Table] = {
    val cbResponse: IO[Either[String, Table]] = Async[IO].async { cb =>
      val mabyTable: Either[String, Table] = for {
        tableNameAndRegion <- config
        table = dynamoDbProxy.at(tableNameAndRegion._1.region).table(tableNameAndRegion._2)
        querriedTable <- (table match {
          case None => Left("Table Doesn't Exist")
          case Some(table) => Right(table)
        })
      } yield querriedTable

      mabyTable match {
        case Left(msg) => cb(Left(new Throwable(msg)))
        case Right(tbl) => cb(Right(Right(tbl)))
      }
    }
    EitherT(cbResponse)
  }

  private def mapConfigToEither(reg:Option[AWSRegion], tabl:Option[String]): Either[String ,Tuple2[AWSRegion, String]] = {
    (reg:Option[AWSRegion], tabl:Option[String]) match {
      case (None, None) => Left("Neither the tablename or region is specificed")
      case (_, None) => Left("The tablename is not specified")
      case (None, _) => Left("The region is not specififed")
      case (Some(reg), Some(tabname)) => Right((reg, tabname))
     }
  }
}
