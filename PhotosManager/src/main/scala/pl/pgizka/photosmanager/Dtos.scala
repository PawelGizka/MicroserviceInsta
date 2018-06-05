package pl.pgizka.photosmanager
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import pl.pgizka.photosmanager.Data.Comment
import spray.json._

object Dtos {
  case class FriendDto(id: Int, username: String)
  case class UserInfoDto(userId: Int, friends: Seq[FriendDto])

  case class CommentDto(friend: FriendDto, commentValue: String, date: Long)

  case class PhotoDto(id: Long, owner: FriendDto, date: Long, photoPath: String, likes: Seq[FriendDto], comments: Seq[CommentDto])

  case class LikePostDto(photoId: Long)
  case class CommentPostDto(photoId: Long, commentValue: String)

  trait JsonDtosSupport extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val friendDtoFormat: RootJsonFormat[FriendDto] = jsonFormat2(FriendDto)
    implicit val userInfoDtoFormat: RootJsonFormat[UserInfoDto] = jsonFormat2(UserInfoDto)
    implicit val commentDtoFormat: RootJsonFormat[CommentDto] = jsonFormat3(CommentDto)
    implicit val photoDtoFormat: RootJsonFormat[PhotoDto] = jsonFormat6(PhotoDto)
    implicit val likePostDtoFormat: RootJsonFormat[LikePostDto] = jsonFormat1(LikePostDto)
    implicit val commentPostDtoFormat: RootJsonFormat[CommentPostDto] = jsonFormat2(CommentPostDto)
  }
}
