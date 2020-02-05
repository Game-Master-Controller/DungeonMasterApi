package com.dungeonMaster.dungeonmasterapi

import cats.effect._
import awscala._, dynamodbv2._
import cats.data.EitherT
import scala.language.higherKinds

/*
TODO: 

1. Rename DynamoDataStore to Async*
2. Refactor tests
3. Refactor method type parameters to class level or rememove class level type
4. Consider errors when putting to table? 
*/

abstract class DataStore[F[+_], B] {
  def createEntry(index: String, values:Option[Seq[(String, Any)]])(implicit dependency: B): EitherT[F, String, String]
}

object DynamoDataStore extends DataStore[IO, DynamoDBFacade[IO]] {
  lazy implicit val dynamo: DynamoDB = DynamoDB()
  lazy val validRegion = implicitly[USEAST1]
  lazy val region: Option[AWSRegion] = Some(validRegion)
  lazy val facade = DynamoDataStoreConfig.apply[IO]
  implicit lazy val connectedFacade = facade.addRegion(region).connect.value.unsafeRunSync().getOrElse(() => throw new Exception("Failed to Connect to Dynamo Table"))

  def createEntry(index: String, values:Option[Seq[(String, Any)]])(implicit dependency: DynamoDBFacade[IO]): EitherT[IO, String, String] = {
    import com.dungeonMaster.dungeonmasterapi.USEAST1
    dependency.submitEntry(index, values)
  }
}

abstract class DynamoDBFacade[F[_]: Async] {
  val dynamoTable: Option[Table]
  val region: Option[AWSRegion]
  val tableName: Option[String]
  def addTableName(tableName: Option[String]): DynamoDBFacade[F]
  def addRegion(region: Option[AWSRegion]): DynamoDBFacade[F]
  def connect(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, DynamoDBFacade[F]]
  def submitEntry(index: String, values:Option[Seq[(String, Any)]])(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, String]
}

object DynamoDataStoreConfig {
  private def constructor[F[+_]: Async](table: Option[Table] = None, givenRegion: Option[AWSRegion] = None, givenTableName: Option[String] = None): DynamoDBFacade[F] = new DynamoDBFacade[F] {
      val dynamoTable: Option[Table] = table
      val region: Option[AWSRegion] = givenRegion
      val tableName: Option[String] = givenTableName
      def addTableName(tableName: Option[String]): DynamoDBFacade[F] = constructor[F](table, givenRegion, tableName)
      def addRegion(providedRegion: Option[AWSRegion]): DynamoDBFacade[F] = constructor[F](table, providedRegion, givenTableName)
      def submitEntry(index: String, values:Option[Seq[(String, Any)]])(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, String] = {
        val mabyTable = dynamoTable match {
          case None => Left("Table not connected")
          case Some(dynamoTable) => Right(dynamoTable)
        }
        val res: F[Either[String, String]] = Async[F].async { cb =>
          val putToTableIfThere = for {
            tbl <- mabyTable
            _ = tbl.put(index, values)
          } yield tbl
          val errorOrDynamodb: Either[String, String] = putToTableIfThere match {
            case Left(errMsg) => Left(errMsg)
            case Right(_) => Right(s"Successful entry of $index")
          }
          cb(Right(errorOrDynamodb))
        }
        EitherT(res)
      }
      def connect(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, DynamoDBFacade[F]] = {
        val config: Either[String ,Tuple2[AWSRegion, String]] = mapConfigToEither(region, tableName)
        val mabyTable: EitherT[F, String, Table] = querryTable[F](config, dynamoDbProxy)
        for {
          tableOrError <- mabyTable
          newDynamoStore = constructor[F](Some(tableOrError), givenRegion, givenTableName)
        } yield newDynamoStore
      }
  }

implicit def apply[F[+_]: Async]: DynamoDBFacade[F] = constructor[F]()

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
