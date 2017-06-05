package todos

import java.sql.Connection
import javax.inject.{Inject, Singleton}
import anorm._
import anorm.SqlParser._
import play.api.db.Database
import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

@Singleton
class TodosRepository @Inject()(db: Database) {

  def withDb[T](body: Connection => T): Future[T] = Future(db.withConnection(body(_)))

  val todoParser: RowParser[Todo] = long("id") ~ str("text") ~ bool("done") map {
    case id ~ text ~ done => Todo(id, text, done)
  }

  val todoCommentParser: RowParser[TodoComment] = long("id") ~ long("todo_id") ~ str("text") map {
    case id ~ todo_id ~ text => TodoComment(id, todo_id, text)
  }

  def create(text: String): Future[Option[Todo]] = withDb { implicit conn =>
    val result: Option[Long] = SQL("INSERT INTO todos (id, text, done) VALUES(DEFAULT, {text}, FALSE)")
      .on("text" -> text)
      .executeInsert()

    result.flatMap(fetchTodoById)
  }

  def createComment(todo_id: Long, text: String): Future[Option[TodoComment]] = withDb { implicit conn =>
    val result: Option[Long] = SQL("INSERT INTO todo_comments (id, text, todo_id) VALUES(DEFAULT, {text}, {todo_id})")
      .on(
        "text" -> text,
        "todo_id" -> todo_id
      )
      .executeInsert()

    result.flatMap(fetchCommentById)
  }

  def getAllComments(todo_id: Long): Future[List[TodoComment]] = withDb { implicit conn =>
    SQL("SELECT * FROM todo_comments WHERE todo_id = {todo_id}")
      .on("todo_id" -> todo_id)
      .as(todoCommentParser.*)
  }


  def update(id: Long, text: Option[String], done: Option[Boolean]): Future[Option[Todo]] = withDb { implicit conn =>
    fetchTodoById(id).map { todo =>
      SQL("UPDATE todos SET text = {text}, done = {done} WHERE id = {id}")
        .on(
          "id" -> id,
          "text" -> text.getOrElse(todo.text),
          "done" -> done.getOrElse(todo.done)
        )
        .executeUpdate()
    } match {
      case Some(x: Int) if x > 0 => fetchTodoById(x)
      case _ => None
    }
  }


  def getOne(id: Long): Future[Option[Todo]] = withDb { implicit conn => fetchTodoById(id) }


  def getAll(done: Option[Boolean]): Future[List[Todo]] = withDb { implicit conn =>
    SQL("SELECT * FROM todos").as(todoParser.*)
  }


  def delete(id: Long): Future[Map[String, Int]] = withDb { implicit conn =>
    val deletedComments: Int = deleteComments(id)
    val deletedTodos: Int = deleteTodo(id)

    Map("deletedComments" -> deletedComments, "deletedTodos" -> deletedTodos)
  }

  private def deleteTodo(id: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM todos WHERE id = {id}")
      .on("id" -> id)
      .executeUpdate()
  }

  private def deleteComments(todo_id: Long)(implicit connection: Connection): Int = {
    SQL("DELETE FROM todo_comments WHERE todo_id = {todo_id}")
      .on("todo_id" -> todo_id)
      .executeUpdate()
  }


  private def fetchTodoById(id: Long)(implicit connection: Connection): Option[Todo] =
    SQL("SELECT * FROM todos WHERE id = {id}")
      .on("id" -> id)
      .as(todoParser.singleOpt)

  private def fetchCommentById(id: Long)(implicit connection: Connection): Option[TodoComment] =
    SQL("SELECT * FROM todo_comments WHERE id = {id}")
      .on("id" -> id)
      .as(todoCommentParser.singleOpt)
}