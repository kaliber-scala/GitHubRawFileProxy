Git raw file proxy
=====================

This simple application can be used to proxy private raw files from A Git provider.

Preparation
-----------

Use the documentation of the API to create an access token that has access with the 
scope `repo` to the target repository.

Github: http://developer.github.com/v3/oauth/#get-a-single-authorization  
Gitlab: https://docs.gitlab.com/ee/api/

Configuration
-------------

``` scala
allowed.accessTokens="3ab423aa747918asdd2a256b852c5a60580d3,3ab423aa7479186c96eb32asd5a60asd0d3"
```

Usage
-----

On the master branch
http://localhost:9000/{accessToken}/{Owner}/{Repository}/{path/to/file.ext}

On a specify branch
http://localhost:9000/?branch={branchName}&accessToken={accessToken}&owner={Owner}&repository={Repository}&path={path/to/file.ext}

The `branch` query param can be omitted which will cause it to fallback to the master branch

HEROKU Deployment
----------
HEROKU_API_KEY="xxxx-xxxx-xxxx-xxxx" sbt stage deployHeroku

Docker Deployment
----------
See https://github.com/marcuslonnberg/sbt-docker for available methods.

Need to override the Images / tag name?  
Create a file named overrideDockerImageName.sbt in the root with the give content:
The registry is optional. When not specified it will use the docker.io registry.

```
lazy val overrideDockerImageNames = settingKey[Option[Seq[ImageName]]]("get the docker image name")
overrideDockerImageNames := Some(Seq(
  ImageName(
    registry = Some("some registry address"),
    repository = "{imageName}:{tag}")
))
```

Typically you want to use something like this:
```
lazy val overrideDockerImageNames = settingKey[Option[Seq[ImageName]]]("get the docker image name")
overrideDockerImageNames := Some(Seq(
  ImageName(s"${organization.value}/${name.value}-${version.value}:latest"),
  ImageName(s"${organization.value}/${name.value}:${version.value}")
))
```

Build the image usage: `sbt dockerBuildAndPush`
Start a docker container:

docker-compose.yml
```
gitrawfileproxy:
  image: {registry}/{imagename}:{tag}
  stdin_open: true
  restart: always
  environment:
    - JAVA_OPTS=-Dconfig.file=/configs/environment.conf
  volumes:
    - ./:/configs
    - /tmp/resource-api:/logs
  ports:
    - "{exposed port}:9000"
```
Make sure that the /config directory is available as a volume and that it contains the environment.conf

To start the container: `docker compose up -d`