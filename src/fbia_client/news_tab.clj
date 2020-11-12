(ns fbia-client.news-tab
  "manage articles in the facebook news-tab"
  (:require [fbia-client.util :refer [get-request
                                      post-request
                                      xf-http-response
                                      xf-json-decode
                                      graph-url
                                      api-version]]
            [clojure.core.async :refer [transduce]])
  (:refer-clojure :exclude [transduce]))

(def parse-response (comp xf-http-response xf-json-decode))

(defn index-article [url scopes access-token & {:keys [update? deny?]}]
  (transduce parse-response conj []
             (post-request (graph-url api-version ""
                                      (merge {:scopes scopes
                                              :id url
                                              :access_token access-token
                                              :denylist (str deny?)}
                                             (when update? {:scrape "true"}))))))

(defn unindex-article [url scopes access-token]
  (index-article url scopes access-token {:deny? true}))

(defn lookup-article [url access-token]
  (transduce parse-response conj []
             (get-request (graph-url api-version ""
                                     {:fields "scopes"
                                      :id url
                                      :access_token access-token}))))
