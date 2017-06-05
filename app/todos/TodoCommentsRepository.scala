package todos

import java.sql.Connection
import javax.inject.{Inject, Singleton}

import anorm.SqlParser._
import anorm._
import play.api.db.Database

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class TodoCommentsRepository @Inject()(db: Database) {

  def withDb[T](body: Connection => T): Future[T] = Future(db.withConnection(body(_)))

  val todoCommentParser: RowParser[TodoComment] = long("id") ~ long("todo_id") ~ str("text") map {
    case id ~ todo_id ~ text => TodoComment(id, todo_id, text)
  }

  def create(todo_id: Long, text: String): Future[Option[TodoComment]] = withDb { implicit conn =>
    val result: Option[Long] = SQL("INSERT INTO todo_comments (id, text, todo_id) VALUES(DEFAULT, {text}, {todo_id})")
      .on(
        "text" -> text,
        "todo_id" -> todo_id
      )
      .executeInsert()

    result.flatMap(fetchCommentById)
  }


  private def fetchCommentById(id: Long)(implicit connection: Connection): Option[TodoComment] =
    SQL("SELECT * FROM todo_comments WHERE id = {id}")
      .on("id" -> id)
      .as(todoCommentParser.singleOpt)
}