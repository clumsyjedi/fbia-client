(ns fbia-client.auth
  (:require [clojure.core.async :refer [chan pipeline pipeline-async]]
            [fbia-client.util
             :refer
             [aform
              get-request
              graph-url
              xf-http-decode
              xf-http-response
              xf-json-decode
              xform-map]]))

(defn extend-token 
  "Given a short-lived user token, generates a long-lived user token"
  [app-id app-secret short-lived-token]
  (let [ch (chan 1)]
    (pipeline 1 ch
              (comp xf-http-response xf-http-decode)
              (get-request (graph-url "/oauth/access_token" 
                                      {:grant_type "fb_exchange_token" 
                                       :client_id app-id
                                       :client_secret app-secret
                                       :fb_exchange_token short-lived-token})))
    ch))

(defn accounts 
  "Loads the accounts page at /me/accounts"
  [extended-token]
  (let [ch (chan 1)] 
    (pipeline 1 ch
              (comp xf-http-response xf-json-decode)
              (get-request (graph-url "/me/accounts" {:access_token extended-token})))
    ch))

(defn page-token-from-user-token [page-id app-id app-secret user-token]
  (let [token-ch (chan 1)
        account-ch (chan 1)
        res (chan 1)]
    (pipeline 1 token-ch (xform-map :access_token)
              (extend-token app-id app-secret user-token))
    (pipeline-async 1 account-ch (aform accounts)
                    token-ch)
    (pipeline 1 res (comp (xform-map :data)
                          (xform-map (fn [x] 
                                       (filter #(= page-id (:id %)) x)))
                          (xform-map first)
                          (xform-map :access_token))
                          account-ch)
              res))

