package controllers

import akka.util.ByteString
import com.google.inject.Inject
import play.api._
import play.api.mvc._
import play.api.libs.ws.WSClient
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.JsValue
import play.api.http.{HttpEntity, MimeTypes}
import org.apache.commons.codec.binary.Base64
import services.UrlProvider

import scala.concurrent.Future

class Application @Inject()(configuration: Configuration, ws: WSClient, urlProvider: UrlProvider) extends Controller {

  def proxy(accessToken: String, owner: String, repository: String, path: String): Action[AnyContent] =
    proxy(accessToken, owner, repository, None, path)

  def proxyWithBranchOption(accessToken: String, owner: String, repository: String, branchOption: Option[String], path: String): Action[AnyContent] =
    proxy(accessToken, owner, repository, branchOption, path)

  private def proxy(accessToken: String, owner: String, repository: String, branchOption: Option[String], path: String): Action[AnyContent] = Action.async {
    val key = "allowed.accessTokens"

    val allowedAccessTokens = configuration
      .getString(key)
      .getOrElse(throw new PlayException("Configuration error", "Could not find " + key + " in configuration"))

    if (allowedAccessTokens.split(",") contains accessToken) {

      val url = urlProvider.url(accessToken, owner, repository, branchOption, path)

      ws
        .url(url)
        .get
        .map { response =>
          val result = (response.status, response.header(CONTENT_TYPE)) match {
            case (200, Some(mimeType)) if mimeType.contains(MimeTypes.JSON) => Right(response.json)
            case (status, mimeType) => Left(s"Problem accessing github api, status '$status', mimetype '$mimeType', ${response.body.toString}")
          }

          result.fold(
            problem => BadRequest(problem),
            json => Ok.sendEntity(HttpEntity.Strict(ByteString(getFileContents(json)), Some(MimeTypes.BINARY)))
          )
        }

    } else Future.successful(BadRequest(s"Access denied for access token '$accessToken'"))
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
  private def getFileContents(json: JsValue): Array[Byte] = {
    val content = (json \ "content").as[String]

    Base64 decodeBase64 content.getBytes
  }

}
