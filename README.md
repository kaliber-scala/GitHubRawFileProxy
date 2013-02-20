GitHub raw file proxy
=====================

This simple application can be used to proxy private raw files from GitHub.

Preparation
-----------

Use the documentation of the API to create an access token that has access with the 
scope `repo` to the target repository.

http://developer.github.com/v3/oauth/#get-a-single-authorization

Configuration
-------------

``` scala
	allowed.accessTokens=[3ab423aa747918asdd2a256b852c5a60580d3,3ab423aa7479186c96eb32asd5a60asd0d3]
```

Usage
-----

http://localhost:9000/accessToken/Owner/Repository/path/to/file.ext

