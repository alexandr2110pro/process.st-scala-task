package todos

import play.api.libs.json.Json

case class Todo(id: Long, text: String, done: Boolean)

object Todo {
  implicit val todoWriter = Json.writes[Todo]
  implicit val todoReader = Json.reads[Todo]
}

