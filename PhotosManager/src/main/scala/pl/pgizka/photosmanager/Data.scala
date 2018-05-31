package pl.pgizka.photosmanager

import java.util.Date

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json._

object Data {
  case class Comment(userId: Int, commentValue: String, date: Long)
  case class Photo(id: Long, ownerId: Int, date: Long, likes: Seq[Int], comments: Seq[Comment])

  trait JsonDataSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val commentFormat: RootJsonFormat[Comment] = jsonFormat3(Comment)
    implicit val photoFormat: RootJsonFormat[Photo] = jsonFormat5(Photo)
  }
}
