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

Deployment
----------
HEROKU_API_KEY="xxxx-xxxx-xxxx-xxxx" sbt stage deployHeroku
