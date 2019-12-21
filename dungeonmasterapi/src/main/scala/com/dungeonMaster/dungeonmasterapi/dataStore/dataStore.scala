package com.dungeonMaster.dungeonmasterapi

import cats.effect._
import awscala._, dynamodbv2._
import cats.data.EitherT
import scala.language.higherKinds

abstract class DataStore[F[_]] {
  def createEntry(values: Map[String, String]): F[NetworkResponse]
}

abstract class DynamoDataStore[F[_]: Async] extends DataStore[F] {
  val dynamoTable: Option[Table]
  val region: Option[AWSRegion]
  val tableName: Option[String]
  def addTableName(tableName: Option[String]): DynamoDataStore[F]
  def addRegion(region: Option[AWSRegion]): DynamoDataStore[F]
  def connect[F[+_]: Async](implicit dynamoDbProxy: DynamoDB): EitherT[F, String, DynamoDataStore[F]]
  def createEntry(values: Map[String, String]): F[NetworkResponse] = ???
}

object DynamoDataStore {

  private def constructor[F[+_]: Async](table: Option[Table] = None, givenRegion: Option[AWSRegion] = None, givenTableName: Option[String] = None): DynamoDataStore[F] = new DynamoDataStore[F] {
      val dynamoTable: Option[Table] = table
      val region: Option[AWSRegion] = givenRegion
      val tableName: Option[String] = givenTableName
      def addTableName(tableName: Option[String]): DynamoDataStore[F] = constructor[F](table, givenRegion, tableName)
      def addRegion(providedRegion: Option[AWSRegion]): DynamoDataStore[F] = constructor[F](table, providedRegion, givenTableName)
      override def createEntry(values: Map[String, String]): F[NetworkResponse] = ???
      def connect[F[+_]: Async](implicit dynamoDbProxy: DynamoDB): EitherT[F, String, DynamoDataStore[F]] = {
        val config: Either[String ,Tuple2[AWSRegion, String]] = mapConfigToEither(region, tableName)
        val mabyTable: EitherT[F, String, Table] = querryTable[F](config, dynamoDbProxy)
        for {
          tableOrError <- mabyTable
          newDynamoStore = constructor[F](Some(tableOrError), givenRegion, givenTableName)
        } yield newDynamoStore
      }
  }
  implicit def apply[F[+_]: Async]: DynamoDataStore[F] = constructor[F]()

  private def querryTable[F[+_]: Async](config: Either[String ,Tuple2[AWSRegion, String]], dynamoDbProxy: DynamoDB): EitherT[F, String, Table] = {
    val cbResponse: F[Either[String, Table]] = Async[F].async { cb =>
      val mabyTable: Either[String, Table] = for {
        tableNameAndRegion <- config
        table = dynamoDbProxy.at(tableNameAndRegion._1.region).table(tableNameAndRegion._2)
        querriedTable <- (table match {
          case None => Left("Table Doesn't Exist")
          case Some(table) => Right(table)
        })
      } yield querriedTable

      mabyTable match {
        case Left(msg) => cb(Right(Left(msg)))
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
