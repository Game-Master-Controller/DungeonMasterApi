package com.dungeonMaster.dungeonmasterapi

import io.circe.Encoder
import io.circe.{Encoder, Decoder, Json, HCursor}
import org.http4s.circe._
import cats.Applicative
import cats.effect.Sync
import cats.implicits._
import io.circe.{Encoder, Decoder, Json, HCursor}
import io.circe.generic.semiauto._
import org.http4s._
import org.http4s.implicits._
import org.http4s.{EntityDecoder, EntityEncoder, Method, Uri, Request}
import org.http4s.client.Client
import org.http4s.client.dsl.Http4sClientDsl
import org.http4s.Method._
import org.http4s.circe._

object Encoders {

  implicit val responseMessageDecoder: Decoder[ResponseMessage] = deriveDecoder
  implicit def responseMessageEntityDecoder[F[_]: Sync]: EntityDecoder[F, ResponseMessage] = jsonOf


  implicit val responseMessageEncoder: Encoder[ResponseMessage] = deriveEncoder
  implicit def responseMessageEntityEncoder[F[_]: Applicative]: EntityEncoder[F, ResponseMessage] = jsonEncoderOf
}
