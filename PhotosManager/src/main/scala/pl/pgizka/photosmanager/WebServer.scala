package pl.pgizka.photosmanager

import java.io.{File, FileInputStream, FileOutputStream}
import java.util.Date
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.{JsonArray, JsonObject}

import scala.io.{Source, StdIn}
import Data._
import Dtos._
import akka.http.scaladsl.server.Directives
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.couchbase.client.java.query.{N1qlQuery, N1qlQueryRow}
import pl.pgizka.photosmanager.Data.Photo
import pl.pgizka.photosmanager.WebServer.fetchUsersInfo
import spray.json._

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration


object WebServer extends Directives with JsonDataSupport with JsonDtosSupport {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  // needed for the future flatMap/onComplete in the end
  implicit val executionContext = system.dispatcher

  def main(args: Array[String]) {

    val cluster = CouchbaseCluster.create("127.0.0.1:8091")
    cluster.authenticate("photosmanager", "adminadmin")

    // Connect to the bucket and open it
    val bucket = cluster.openBucket("photosmanager")
    // Create a N1QL Primary Index (but ignore if it exists)
    bucket.bucketManager().createN1qlPrimaryIndex(true, false);

    // Create a JSON document and store it with the ID "helloworld"
    val content = JsonObject.create.put("hello", "world")
    val inserted = bucket.upsert(JsonDocument.create("helloworld", content))

    // Read the document and print the "hello" field
    val found = bucket.get("helloworld")
    System.out.println("Couchbase is the best database in the " + found.content.getString("hello"))


    val route =
      pathPrefix("photos") {
        get {
          extractRequest { request =>
            onSuccess(fetchUsersInfo(request)) { result =>

              val ids =
                if (result.friends.isEmpty) ""
                else result.friends.map(_.id).tail.foldLeft(result.friends.get(0).id.toString)(_ + ", " + _)
              println(ids)
              val query = s"select id, comments, date, likes, ownerId  from photosmanager where ownerId in [$ids] order by date desc;"
              val parsed = bucket.query(N1qlQuery.simple(query)).allRows().toList.map(_.value().toString.parseJson.convertTo[Photo])

              val friendsMap = result.friends.map(dto => dto.id -> dto).toMap

              val dtos = parsed.map { photoData =>
                val comments = photoData.comments.map{ commentData =>
                  CommentDto(friendsMap(commentData.userId), commentData.commentValue, commentData.date)
                }
                val likes = photoData.likes.map(id => friendsMap(id))
                val photoPath = s"SomePathToPhoto ${photoData.id}"
                PhotoDto(photoData.id, photoData.ownerId, photoData.date, photoPath, likes, comments)
              }

              complete(dtos)
            }
          }
        } ~
        post {
          extractRequest { request =>
            uploadedFile("photo") {
              case (metadata, file) =>

                onSuccess(fetchUsersInfo(request)) { userInfo =>

                  val nextId = bucket.counter("photosCounter", 1, 0)
                  val photoId = "photo:" + nextId.content()
                  val photo = Photo(nextId.content(), 2, new Date().getTime, List(), List())
                  bucket.upsert(JsonDocument.create(photoId, JsonObject.fromJson(photo.toJson.toString())))

                  val outFile = new File(s"photo:${nextId.content()}")

                  new FileOutputStream(outFile).getChannel().transferFrom(new FileInputStream(file).getChannel, 0, Long.MaxValue )
                  file.delete()

                  complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
                }
            }
          }
        } ~
        path("like") {
          post {
            entity(as[LikePostDto]) { likePostDto: LikePostDto =>
              extractRequest { request =>
                onSuccess(fetchUsersInfo(request)) { userInfo =>

                  val document = bucket.get(s"photo:${likePostDto.photoId}")
                  val likes = document.content().getArray("likes")
                  val contains = 0.until(likes.size()).foldLeft(false)(_ || likes.getInt(_) == userInfo.userId)
                  if (!contains) {
                    likes.add(userInfo.userId)
                    bucket.replace(document)
                  }

                  complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))

                }
              }
            }
          }
        } ~
        path("comment") {
          post {
            entity(as[CommentPostDto]) { commentPostDto: CommentPostDto =>
              extractRequest { request =>
                onSuccess(fetchUsersInfo(request)) { userInfo =>

                  val document = bucket.get(s"photo:${commentPostDto.photoId}")
                  val comments = document.content().getArray("comments")
                  val comment = Comment(userInfo.userId, commentPostDto.commentValue, new Date().getTime).toJson.toString()
                  comments.add(JsonObject.fromJson(comment))

                  bucket.replace(document)

                  complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h1>Say hello to akka-http</h1>"))
                }
              }
            }
          }
        }
      //TODO this will be in Users microservice
      } ~
      pathPrefix("users") {
        path("info") {
          get {
            extractRequest { request =>
              complete(UserInfoDto(1, Seq(FriendDto(1, "pawel", "Gizka"), FriendDto(2, "jan", "kowalski"))))
            }
          }
        }
      }

    val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

    println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
  }

  def fetchUsersInfo(request: HttpRequest): Future[UserInfoDto] = {
    if (request.headers.map(_.name()).contains("token")) {
      Http().singleRequest(HttpRequest(uri = "http://localhost:8080/users/info"))
        .flatMap{response =>
          response.entity.toStrict(FiniteDuration(5, TimeUnit.SECONDS)).map(_.data.utf8String.parseJson.convertTo[UserInfoDto])
        }
    } else {
      Future.failed(new Exception("No token in request"))
    }
  }
}
