package controllers

import play.api._
import play.api.mvc._
import play.api.libs.ws.WS
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.libs.iteratee.Enumerator
import org.apache.commons.codec.binary.Base64
import play.api.Play.current
import scala.concurrent.Future

object Application extends Controller {

  def index = Action {
    Ok(views.html.index("GitHub Raw File Proxy"))
  }

  /*
   {
  	"sha": "dabc62e2fde3dd60547f17ff8a0010484f173928",
  	"size": 301,
  	"name": "file.ext",
  	"path": "path/file.ext",
  	"type": "file",
  	"url": "https://api.github.com/repos/Rhinofly/repo/contents/path/file.ext",
  	"git_url": "https://api.github.com/repos/Rhinofly/repo/git/blobs/dabc62e2fde3dd60547f17ff8a0010484f173928",
  	"html_url": "https://github.com/Rhinofly/repo/blob/master/path/file.ext",
  	"_links": {
    	"self": "https://api.github.com/repos/Rhinofly/repo/contents/path/file.ext",
    	"git": "https://api.github.com/repos/Rhinofly/repo/git/blobs/dabc62e2fde3dd60547f17ff8a0010484f173928",
    	"html": "https://github.com/Rhinofly/repo/blob/master/path/file.ext"
  	},
  	"content": "base64encodedString",
  	"encoding": "base64"
	}
   */
  def getFileContents(json: JsValue): Array[Byte] = {
    val content = (json \ "content").as[String]

    Base64 decodeBase64 content.getBytes
  }

  def proxy(accessToken: String, owner: String, repository: String, path: String) = Action.async {

    val key = "allowed.accessTokens"

    val app = implicitly[Application]
    val allowedAccessTokens = app.configuration
      .getString(key)
      .getOrElse(throw new PlayException("Configuration error", "Could not find " + key + " in configuration"))

    if (allowedAccessTokens.split(",") contains accessToken) {

      val url =
        s"https://api.github.com/repos/$owner/$repository/contents/$path?access_token=$accessToken"

      WS
        .url(url)
        .get
        .map { response =>
          val result = response.status match {
            case 200 => Right(response.json)
            case status => Left(s"Problem accessing github api, status '$status', $response.body")
          }

          result.fold(
            problem => BadRequest(problem),
            json =>
              Result(
                header = ResponseHeader(200, Map(CONTENT_TYPE -> "application/octet-stream")),
                body = Enumerator(getFileContents(json))))
        }

    } else Future.successful(BadRequest(s"Access denied for access token '$accessToken'"))
  }

}