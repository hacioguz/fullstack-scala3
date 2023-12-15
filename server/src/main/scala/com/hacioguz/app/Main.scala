package com.hacioguz.app

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.IOApp
import cats.effect.*
import com.comcast.ip4s.*
import com.hacioguz.app.model.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.dsl.io.*
import org.http4s.ember.server.EmberServerBuilder
import org.http4s.implicits.*
import org.http4s.server.{Router, Server}

object Main extends IOApp.Simple:

  // 1: Create the product service
  private val productService: HttpRoutes[IO] = HttpRoutes.of[IO] {
    case GET -> Root / "products" =>
      Ok(ProductDAO.findProducts().asJson)
  }
  // 2: Allocate a route to the service in the router
  private val httpApp: Kleisli[IO, Request[IO], Response[IO]] = Router("/api" -> productService).orNotFound

  // 3: Build the actual server
  private val server: Resource[IO, Server] = EmberServerBuilder
    .default[IO]
    .withHost(ipv4"0.0.0.0")
    .withPort(port"8080")
    .withHttpApp(httpApp)
    .build

  // 4: Launch the server in the application loop
  val run: IO[Unit] = for {
    _ <- server.allocated
    _ <- IO.never // this is needed so that the server keeps running
  } yield ()

