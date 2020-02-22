package com.dungeonMaster.dungeonmasterapi

import cats.effect._
import awscala._, dynamodbv2._
import cats.data.EitherT
import scala.language.higherKinds
import com.typesafe.config.Config
import com.dungeonMaster.dungeonmasterapi.TableNames.Games
import cats.Functor

/*
1. Rename DynamoDataStore to Async*
2. Refactor tests
3. Refactor method type parameters to class level or rememove class level type
4. Consider errors when putting to table? 
 */

object TableNames {
  import com.dungeonMaster.dungeonmasterapi.config.config

  abstract class DynamoTableName {
    val name: String
  }

  case object Games extends DynamoTableName {
    override val name = config.getString("game-table.name")
  }
}

abstract class DataStore[F[_]: Async] {
  def createEntry(index: String, values:Option[Seq[(String, Any)]]): EitherT[F, String, String]
}

object DataStore {
  lazy implicit val dynamo: DynamoDB = DynamoDB()
  lazy val config = com.dungeonMaster.dungeonmasterapi.config.config
  lazy val region: Option[AWSRegion] = Some(USEAST1)
  implicit def getConnectedDb[F[_]: Async:ConcurrentEffect]: DynamoDBFacade[F] = {
    lazy val facade: DynamoDBFacade[F] = DynamoDataStoreConfig.apply[F].addRegion(region).addTableName(Some(config.getString("game-table.name"))).connect[F, IO].value.unsafeRunSync().
      getOrElse(throw new Exception("Error Connecting to The DB"))
    facade
  }
  implicit def apply[F[_]: Async](implicit facade: DynamoDBFacade[F]) = new DataStore[F] {    
    def createEntry(index: String, values:Option[Seq[(String, Any)]]): EitherT[F, String, String] = {
      import com.dungeonMaster.dungeonmasterapi.USEAST1
      facade.submitEntry(index, values)
    }
  }
}

abstract class DynamoDBFacade[F[_]: Async] {
  val dynamoTable: Option[Table]
  val region: Option[AWSRegion]
  val tableName: Option[String]
  def addTableName(tableName: Option[String]): DynamoDBFacade[F]
  def addRegion(region: Option[AWSRegion]): DynamoDBFacade[F]
  def connect[F[_]: ConcurrentEffect:Functor, F1[_]:Functor:Sync](implicit dynamoDbProxy: DynamoDB): EitherT[F1, String, DynamoDBFacade[F]]
  def submitEntry(index: String, values:Option[Seq[(String, Any)]])(implicit dynamoDbProxy: DynamoDB): EitherT[F, String, String]
}

object DynamoDataStoreConfig {

  private def constructor[F[_]: Async](table: Option[Table] = None, givenRegion: Option[AWSRegion] = None, givenTableName: Option[String] = None): DynamoDBFacade[F] = new DynamoDBFacade[F] {
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
             _ = values match {
               case None => tbl.putItem(hashPK = index)
               case Some(attributes) => tbl.put(hashPK = index, attributes = attributes:_*)
            }
          } yield tbl
          val errorOrDynamodb: Either[String, String] = putToTableIfThere match {
            case Left(errMsg) => Left(errMsg)
            case Right(_) => Right(s"Successful entry of $index")
          }
          cb(Right(errorOrDynamodb))
        }
        EitherT(res)
      }
      def connect[F[_]: ConcurrentEffect:Functor, F1[_]:Functor:Sync](implicit dynamoDbProxy: DynamoDB): EitherT[F1, String, DynamoDBFacade[F]] = {
        val config: Either[String ,Tuple2[AWSRegion, String]] = mapConfigToEither(region, tableName)
        val mabyTable: EitherT[F1, String, Table] = querryTable[F1](config, dynamoDbProxy)
        for {
          tableOrError <- mabyTable
          newDynamoStore = constructor[F](Some(tableOrError), givenRegion, givenTableName)
        } yield newDynamoStore
      }
  }

implicit def apply[F[_]:Async]: DynamoDBFacade[F] = constructor[F]()

  private def querryTable[F[_]:Sync](config: Either[String ,Tuple2[AWSRegion, String]], dynamoDbProxy: DynamoDB): EitherT[F, String, Table] = {
    val cbResponse: F[Either[String, Table]] = Sync[F].delay {
      for {
        tableNameAndRegion <- config
        table = dynamoDbProxy.at(tableNameAndRegion._1.region).table(tableNameAndRegion._2)
        querriedTable <- (table match {
          case None => Left("Table Doesn't Exist")
          case Some(table) => Right(table)
        })
      } yield querriedTable
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
