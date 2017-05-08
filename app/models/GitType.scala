package models

object GitType extends Enumeration {
  type GitType = Value
  val GITHUB, GITLABV3, GITLABV4 = Value

  val default = GITHUB

  def apply(s: String): Value =
    values.find(s.toLowerCase == _.toString.toLowerCase).getOrElse(default)

  def apply(s: Option[String]): Value = s.map(apply).getOrElse(default)
}