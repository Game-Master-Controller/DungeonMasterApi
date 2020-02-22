package com.dungeonMaster.dungeonmasterapi

import cats.Applicative
import cats.implicits._
import io.circe.{Encoder, Json}
import org.http4s.EntityEncoder
import org.http4s.circe._

object stuff {}

/*
trait Game[F[_]]{
  def create(n: Game.GameName)(): F[Game.ResponseMessage] 
}

// TODO: pull out the encoders into a common place

object Game {
  implicit val responseEncoder: Encoder[Game.ResponseMessage] = new Encoder[Game.ResponseMessage] {
    final def apply(a: Game.ResponseMessage): Json = Json.obj(
      ("message", Json.fromString(a.msg)),
    )
  }

  implicit def entityEncoder[F[_]: Applicative]: EntityEncoder[F, Game.ResponseMessage] =
      jsonEncoderOf[F, ResponseMessage]

  final case class GameName(name: String)
  final case class ResponseMessage(msg: String) extends AnyVal

 /* implicit def apply[F[_]: Applicative]: Game[F] = new Game[F] {
    def create(n: Game.GameName): F[Game.ResponseMessage] = ???
  }
  */
}
 */
