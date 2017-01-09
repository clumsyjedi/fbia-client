# fbia-client

It's an FBIA client in clojure

## Obtention

```
:dependencies [[fbia-client "0.4.5"]]
```

## Usage

This client wraps up a lot of the boilerplate stuff for FBIA into a native clojure library. It is not very exciting, but should be a decent enough starting point for devlopers who are working in this space.

Most functions return core.async channels containing one item. If the result is `Throwable` then an error has occurred in the belly of the internet. Otherwise it should contain some data from Facebook about the thig you did.

So creating an FBIA doc and getting the ID of the import would be something like:
```
(:id (<!! (create-article page-id {:keys [html_source access_token], :as params})))
```
I will probably never again update this README, so you should poke around on clojars to look for later versions. I put some API docs under https://github.com/clumsyjedi/fbia-client/tree/master/doc to illustrate the layout.

## Long lived page tokens

I personally struggled generating the long-lived page token for FBIA publishing. If you also struggle with this, you can call [page-token-from-user-token](https://github.com/clumsyjedi/fbia-client/blob/master/src/fbia_client/auth.clj#L35) from a repl, assuming you have the requisite app IDs etc.

## License

Copyright © 2016 Frazer Irving

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
