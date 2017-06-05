import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.execute.AsResult
import org.junit.runner._
import org.specs2.execute
import play.api.libs.json.{JsValue, Json}
import play.api.test._
import play.api.test.Helpers._

import play.api.db.evolutions.Evolutions
import play.api.db.{Database, Databases}


/*
I've wrote only acceptance tests for now.
That was enough for developing in TDD.

I'd like to learn more about play 2.6 before write any real *UNIT* tests with proper mocks & etc.
*/

@RunWith(classOf[JUnitRunner])
class TodosSpec extends Specification {

  /*  TODO: Find out how to do that properly
      This thing will clear db and looks ugly.
      There should be a way to setup/teardown migrations easier
      and use separate "test" db with separate config instead of the "real" one.  */
  abstract class WithDbData extends WithApplication {
    override def around[T: AsResult](t: => T): execute.Result = super.around {
      beforeEach()
      t
    }

    def withMyDatabase[T](block: Database => T): T = {
      Databases.withDatabase(
        driver = "org.postgresql.Driver",
        url = "jdbc:postgresql://localhost/process-st",
        name = "default"
      )(block)
    }

    def beforeEach(): Unit = withMyDatabase { db =>
      println("before each")
      Evolutions.cleanupEvolutions(db)
      Evolutions.applyEvolutions(db)
    }
  }


  "GET /v1/todos/2" should {
    "return the Do Bar todo" in new WithDbData {
      val result = route(app, FakeRequest(GET, "/v1/todos/2")).get
      val expected: JsValue = Json.parse(
        """
          |{
          |  "id": 2,
          |  "text": "Do Bar",
          |  "done": false
          |}
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }
  }

  "GET /v1/todos/3" should {
    "return the Do Baz todo" in new WithDbData {
      val result = route(app, FakeRequest(GET, "/v1/todos/3")).get

      val expected: JsValue = Json.parse(
        """
          |{
          |  "id": 3,
          |  "text": "Do Baz",
          |  "done": true
          |}
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }
  }

  "GET /v1/todos/5" should {
    "return 404" in new WithDbData {
      val result = route(app, FakeRequest(GET, "/v1/todos/5")).get
      status(result) must equalTo(NOT_FOUND)
    }
  }

  "GET /v1/todos" should {

    "return all todos" in new WithDbData {
      val result = route(app, FakeRequest(GET, "/v1/todos")).get
      val expected: JsValue = Json.parse(
        """
          |[
          |  {
          |    "id": 1,
          |    "text": "Do Foo",
          |    "done": false
          |  },
          |  {
          |    "id": 2,
          |    "text": "Do Bar",
          |    "done": false
          |  },
          |  {
          |    "id": 3,
          |    "text": "Do Baz",
          |    "done": true
          |  },
          |  {
          |    "id": 4,
          |    "text": "Do Quix",
          |    "done": true
          |  }
          |]
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }
  }

  "POST /v1/todos" should {

    "return the newly created todo" in new WithDbData {
      val body: JsValue = Json.parse(
        """
          |{
          |   "text": "new todo"
          |}
        """.stripMargin)

      val result = route(app, FakeRequest(POST, "/v1/todos").withJsonBody(body)).get
      val expected: JsValue = Json.parse(
        """
          |{
          |  "id": 5,
          |  "text": "new todo",
          |  "done": false
          |}
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }
  }


  "PUT /v1/todos/1" should {
    "return the updated todo" in new WithDbData {
      val body: JsValue = Json.parse(
        """
          |{
          |   "text": "Updated Foo",
          |   "done": true
          |}
        """.stripMargin)

      val result = route(app, FakeRequest(PUT, "/v1/todos/1").withJsonBody(body)).get

      val expected: JsValue = Json.parse(
        """
          |{
          |  "id": 1,
          |  "text": "Updated Foo",
          |  "done": true
          |}
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)

    }
  }


  "PUT /v1/todos/5" should {
    "return 404" in new WithDbData {
      val body: JsValue = Json.parse(
        """
          |{
          |   "text": "Updated Foo",
          |   "done": true
          |}
        """.stripMargin)

      val result = route(app, FakeRequest(PUT, "/v1/todos/5").withJsonBody(body)).get

      status(result) must equalTo(NOT_FOUND)
    }
  }


  "POST /v1/todos/1/comments" should {
    "return the newly created todo comment" in new WithDbData {
      val body: JsValue = Json.parse(
        """
          |{
          |   "text": "new Foo Todo comment"
          |}
        """.stripMargin)

      val result = route(app, FakeRequest(POST, "/v1/todos/1/comments").withJsonBody(body)).get
      val expected: JsValue = Json.parse(
        """
          |{
          |  "id": 5,
          |  "text": "new Foo Todo comment",
          |  "todo_id": 1
          |}
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }
  }

  "GET /v1/todos/1/comments" should {
    "return comments for the Do Foo todo ('Foo Comment 1' & 'Foo Comment 2')" in new WithDbData {
      val result = route(app, FakeRequest(GET, "/v1/todos/1/comments")).get
      val expected: JsValue = Json.parse(
        """
          |[
          | {
          |   "id": 1,
          |   "todo_id": 1,
          |   "text": "Foo Comment 1"
          | },
          | {
          |   "id": 2,
          |   "todo_id": 1,
          |   "text": "Foo Comment 2"
          | }
          |]
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }
  }


  "DELETE /v1/todos/1" should {
    "return the count of deleted todos and the count of deleted comments" in new WithDbData {
      val result = route(app, FakeRequest(DELETE, "/v1/todos/1")).get
      val expected: JsValue = Json.parse(
        """
          |{
          |  "deletedComments": 2,
          |  "deletedTodos": 1
          |}
        """.stripMargin)

      status(result) must equalTo(OK)
      Json.parse(contentAsString(result)) must beEqualTo(expected)
    }


    "remove the todo and coresponding comments" in new WithDbData {
      val result = route(app, FakeRequest(DELETE, "/v1/todos/1")).get
      status(result) must equalTo(OK)


      val deletedTodo = route(app, FakeRequest(GET, "/v1/todos/1")).get
      status(deletedTodo) must equalTo(NOT_FOUND)

      val comments = route(app, FakeRequest(GET, "/v1/todos/1/comments")).get
      status(comments) must equalTo(OK)
      Json.parse(contentAsString(comments)) must beEqualTo(Json.parse("[]"))
    }
  }

}
