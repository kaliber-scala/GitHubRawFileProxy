package modules

import com.google.inject.AbstractModule
import play.api.{Configuration, Environment}
import services.UrlProvider

class ApplicationLoader(environment: Environment, configuration: Configuration) extends AbstractModule {

  override def configure() = {
    bind(classOf[UrlProvider]).toInstance(UrlProvider.fromConfiguration(configuration))
  }
}