package todos

import play.api.libs.json.Json

case class TodoComment(id: Long, todo_id: Long, text: String)

object TodoComment {
  implicit val todoCommentWriter = Json.writes[TodoComment]
  implicit val todoCommentReader = Json.reads[TodoComment]
}