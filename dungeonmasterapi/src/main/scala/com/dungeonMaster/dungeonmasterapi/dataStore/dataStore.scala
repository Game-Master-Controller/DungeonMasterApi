package com.dungeonMaster.dungeonmasterapi

import cats.effect._
import awscala._, dynamodbv2._
import cats.data.EitherT
import scala.language.higherKinds
import cats.Monad
import cats.Applicative
/*
TODO: 

*
1. Refactor tests
2. Refactor method type parameters to class level or rememove class level type
3. Consider errors when putting to table? 
*/

abstract class DataStore[F[_]] {
  def createEntry[F[+_]: Monad](index: String, values:(String, Any)* )(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, String]
}

object DataStore {
  //implicit def apply(implicit )
}

abstract class DynamoDataStore[F[_]: Monad] extends DataStore[F] {
  val dynamoTable: Option[Table]
  val region: Option[AWSRegion]
  val tableName: Option[String]
  def addTableName(tableName: Option[String]): DynamoDataStore[F]
  def addRegion(region: Option[AWSRegion]): DynamoDataStore[F]
  def connect[F[+_]: Monad](implicit dynamoDbProxy: DynamoDB): EitherT[F, String, DynamoDataStore[F]]
  def createEntry[F[+_]: Monad](index: String, values:(String, Any)* )(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, String]
}

object DynamoDataStore {

  private def constructor[F[+_]: Monad](table: Option[Table] = None, givenRegion: Option[AWSRegion] = None, givenTableName: Option[String] = None): DynamoDataStore[F] = new DynamoDataStore[F] {
      val dynamoTable: Option[Table] = table
      val region: Option[AWSRegion] = givenRegion
      val tableName: Option[String] = givenTableName
      def addTableName(tableName: Option[String]): DynamoDataStore[F] = constructor[F](table, givenRegion, tableName)
      def addRegion(providedRegion: Option[AWSRegion]): DynamoDataStore[F] = constructor[F](table, providedRegion, givenTableName)
      def createEntry[F[+_]: Monad](index: String, values:(String, Any)* )(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, String] = {
        val mabyTable = dynamoTable match {
          case None => Left("Table not connected")
          case Some(dynamoTable) => Right(dynamoTable)
        }

        val res: F[Either[String, String]] = {
          Applicative[F].pure({
            val putToTableIfThere = for {
              tbl <- mabyTable
              _ = tbl.put(index, values)
            } yield tbl

            putToTableIfThere match {
              case Left(errMsg) => Left(errMsg)
              case Right(_) => Right(s"Successful entry of $index")
            }
          })
        }
        EitherT(res)
      }

      def connect[F[+_]: Monad](implicit dynamoDbProxy: DynamoDB): EitherT[F, String, DynamoDataStore[F]] = {
        val config: Either[String ,Tuple2[AWSRegion, String]] = mapConfigToEither(region, tableName)
        val mabyTable: EitherT[F, String, Table] = querryTable[F](config, dynamoDbProxy)
        for {
          tableOrError <- mabyTable
          newDynamoStore = constructor[F](Some(tableOrError), givenRegion, givenTableName)
        } yield newDynamoStore
      }
  }
  implicit def apply[F[+_]: Monad]: DynamoDataStore[F] = constructor[F]()

  private def querryTable[F[+_]: Monad](config: Either[String ,Tuple2[AWSRegion, String]], dynamoDbProxy: DynamoDB): EitherT[F, String, Table] = {
    val cbResponse: F[Either[String, Table]] = Applicative[F].pure {
      val mabyTable: Either[String, Table] = for {
        tableNameAndRegion <- config
        table = dynamoDbProxy.at(tableNameAndRegion._1.region).table(tableNameAndRegion._2)
        querriedTable <- (table match {
          case None => Left("Table Doesn't Exist")
          case Some(table) => Right(table)
        })
      } yield querriedTable

      mabyTable match {
        case Left(msg) => Left(msg)
        case Right(tbl) => Right(tbl)
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
