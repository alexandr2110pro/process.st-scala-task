package controllers

import javax.inject.Inject

import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import todos.{TodoComment, TodoCommentsRepository, TodosRepository}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Todos @Inject()(repository: TodosRepository, commentsRepository: TodoCommentsRepository) extends Controller {

  def findAll(done: Option[Boolean]): Action[AnyContent] = Action.async {

    // TODO: implement `done` filter support
    repository.getAll(done) map { todos =>
      Ok(Json.toJson(todos))
    }
  }

  def find(id: Long): Action[AnyContent] = Action.async {
    repository.getOne(id) map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None => NotFound
    }
  }

  def add: Action[JsValue] = Action.async(parse.json) { implicit request =>
    val text: String = (request.body \ "text").as[String]

    repository.create(text) map { todo =>
      Ok(Json.toJson(todo))
    }
  }

  def update(id: Long): Action[JsValue] = Action.async(parse.json) { implicit request =>
    val text = (request.body \ "text").asOpt[String]
    val done = (request.body \ "done").asOpt[Boolean]

    repository.update(id, text, done) map {
      case Some(todo) => Ok(Json.toJson(todo))
      case None => NotFound
    }
  }

  def remove(id: Long): Action[AnyContent] = Action.async {
    repository.delete(id) map { deleted =>
      Ok(Json.toJson(deleted))
    }
  }


  def addComment(id: Long) = Action.async(parse.json) { implicit request =>
    val text = (request.body \ "text").as[String]

    /* TODO: Why is this not working?

    repository.getOne(id) map {
      case Some(todo) => commentsRepository.create(todo_id = todo.id, text) map { todoComment =>
         Ok(Json.toJson(todoComment))
      }
      case None => NotFound
    }
    */

    repository.createComment(id, text) map { todoComment =>
      Ok(Json.toJson(todoComment))
    }
  }


  def findAllComments(id: Long): Action[AnyContent] = Action.async {
    repository.getAllComments(id) map { todoComments =>
      Ok(Json.toJson(todoComments))
    }
  }
}