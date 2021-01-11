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

(defn- reducer [& xs]
  (last xs))

(def parse-response (comp xf-http-response xf-json-decode))

(defn index-article [url scopes access-token & {:keys [update? deny?]}]
  (transduce parse-response reducer []
             (post-request (graph-url api-version ""
                                      (merge {:scopes scopes
                                              :id url
                                              :access_token access-token}
                                             (when deny? {:denylist "true"})
                                             (when update? {:scrape "true"}))))))

(defn unindex-article [url scopes access-token]
  (index-article url scopes access-token :deny? true))

(defn lookup-article [url access-token]
  (transduce parse-response reducer []
             (get-request (graph-url api-version ""
                                     {:fields "scopes"
                                      :id url
                                      :access_token access-token}))))
