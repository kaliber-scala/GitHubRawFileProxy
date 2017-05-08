package services

import models.GitType
import models.GitType._
import play.api.Configuration
import UrlProvider._

trait UrlProvider {
  def url(accessToken: String, owner: String, repository: String, branchOption: Option[String], path: String): String
}

case class GithubUrlProvider(endPoint: String) extends UrlProvider {
  def url(accessToken: String, owner: String, repository: String, branchOption: Option[String], path: String): String = {
    val branch = branchOption.getOrElse(defaultBranch)

    s"$endPoint/repos/$owner/$repository/contents/$path?ref=$branch&access_token=$accessToken"
  }
}

case class GitlabV4UrlProvider(endPoint: String) extends UrlProvider {
  def url(accessToken: String, owner: String, repository: String, branchOption: Option[String], path: String): String = {
    val branch = branchOption.getOrElse(defaultBranch)

    s"$endPoint/api/v4/projects/$owner%2F$repository/repository/files/${path}?ref=$branch&access_token=$accessToken"
  }

}
case class GitlabV3UrlProvider(endPoint: String) extends UrlProvider {
  def url(accessToken: String, owner: String, repository: String, branchOption: Option[String], path: String): String = {
    val branch = branchOption.getOrElse(defaultBranch)

    s"$endPoint/api/v3/projects/$owner%2F$repository/repository/files?file_path=${path}&ref=$branch&access_token=$accessToken"
  }
}

object UrlProvider {
  val defaultBranch = "master"

  def fromConfiguration(configuration: Configuration): UrlProvider = {
    val gitEndPoint = configuration.getString("git.endPoint").getOrElse("https://api.github.com")
    val gitType = GitType(configuration.getString("git.type"))

    getProvider(gitType)(gitEndPoint)
  }

  def getProvider: GitType => String => UrlProvider = {
    case GITHUB => GithubUrlProvider
    case GITLABV3 => GitlabV3UrlProvider
    case GITLABV4 => GitlabV4UrlProvider
    case _ => throw new RuntimeException(s"GitType not supported")
  }
}