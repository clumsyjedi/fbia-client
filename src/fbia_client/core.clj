(ns fbia-client.core
  (:require [clojure.core.async :refer [chan pipeline]]
            [fbia-client.util
             :refer
             [delete-request
              get-request
              graph-url
              post-request
              xf-http-response
              xf-json-decode]]))

(defn list-articles
  "Retrieve a list of instant articles for a page"
  [page-id {:keys [access_token] :as params}]
  (let [res (chan 1)]
    (pipeline 1 res 
              (comp xf-http-response xf-json-decode)
              (get-request (graph-url "2.6" 
                                      (str "/" page-id "/instant_articles") 
                                      params)))
    res))

(defn lookup-article
  "Retrieve a specific instant article by canonical URL"
  [{:keys [access_token fields id] :as params}]
  (let [res (chan 1)]
    (pipeline 1 res 
              (comp xf-http-response xf-json-decode)
              (get-request (graph-url 2.6 "" 
                                       params)))
    res))

(defn get-article
  "Retrieve a specific instant article by instant article id"
  [id  {:keys [access_token] :as params}]
  (let [res (chan 1)]
    (pipeline 1 res 
              (comp xf-http-response xf-json-decode)
              (get-request (graph-url 2.6 (str "/" id)
                                      params)))
    res))

(defn create-article [page-id {:keys [html_source access_token] :as params}]
  (let [res (chan 1)]
    (pipeline 1 res
              (comp xf-http-response xf-json-decode)
              (post-request (graph-url 2.6 (str "/" page-id "/instant_articles") params)))
    res))

(defn import-status [import-status-id {:keys [access_token] :as params}]
  (let [res (chan 1)] 
    (pipeline 1 res
              (comp xf-http-response xf-json-decode)
              (get-request (graph-url 2.6 (str "/" import-status-id) params)))
    res))

(defn delete-article [article-id {:keys [access_token] :as params}]
  (let [res (chan 1)] 
    (pipeline 1 res
              (comp xf-http-response xf-json-decode)
              (delete-request (graph-url 2.6 (str "/" article-id) params)))
    res))

