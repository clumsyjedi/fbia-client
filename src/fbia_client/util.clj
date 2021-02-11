(ns fbia-client.util
  (:require [clojure.core.async :refer [<! >! chan close! go]]
            [clojure.data.json :as json]
            [org.httpkit.client :as http])
  (:import java.nio.charset.Charset
           org.apache.http.client.utils.URLEncodedUtils
           org.apache.http.message.BasicNameValuePair))

(def api-version "7.0")

(defn ncpus  []
  (.availableProcessors  (Runtime/getRuntime)))

(defn parallelism  []
  (+  (ncpus) 1))

(defn ^{:dynamic true
        :doc "A function called on the input args of any http
             request fn (like get-request). Allows logging of raw
             before they're passed to the low level HTTP libs"}
  *pre-http-fn*
  [params] nil)

(defn aform
  "async fn (see pipeline-async) that catches exceptions and passes through throwables"
  [f]
  (fn [item out*]
    (go (if (instance? Throwable item)
          (>! out* item)
          (>! out* (try (<! (f item))
                        (catch Throwable e e))))
        (close! out*))))

(defn xform-map
  "map transducer that catches exceptions and passes through throwables"
  [f]
  (map (fn [x]
         (if (instance? Throwable x)
           x
           (try (f x)
                (catch Throwable e e))))))
(defn xform-filter
  "filter transducer that catches exceptions and passes through throwables"
  [f]
  (filter (fn [x]
            (if (instance? Throwable x)
              x
              (try (f x)
                   (catch Throwable e e))))))

(defn- encode-params
  "Takes a map of keyword -> string and returns an HTTP URL query string"
  [params]
  (URLEncodedUtils/format (map (fn [[k v]] (BasicNameValuePair. (name k) v)) params)
                          (Charset/forName "UTF-8")))

(defn- decode-params
  "Takes an HTTP URL query string and returns a map of keyword -> string"
  [params]
  (into {} (map (fn [name-value] [(keyword (.getName name-value)) (.getValue name-value)])
                (URLEncodedUtils/parse params (Charset/forName "UTF-8")))))

(def ^{:doc "Transducer to handle http responses"}
  xf-http-response (xform-map (fn [{:keys [body status error]}]
                                (if (or error (< status 200) (> status 299))
                                  (ex-info "HTTP Failed" {:status status :body body} error)
                                  body))))


(def ^{:doc "Transducer to handle JSON decoding"}
  xf-json-decode (xform-map #(json/read-str % :key-fn keyword)))

(def ^{:doc "transducer to handle HTTP query string decoding"}
  xf-http-decode (xform-map #(decode-params %)))

(def ^{:dynamic true :doc "transducer that compbines http and json decoding
                          BUT it's dynamic so it can be overloaded by the caller"}
  *xf-standard* (comp xf-http-response xf-json-decode))

(defn graph-url
  ([path params]
   (str "https://graph.facebook.com" path "?" (encode-params params)))
  ([version path params]
   (str "https://graph.facebook.com/v" version path "?" (encode-params params))))

(defn get-request
  "Makes GET HTTP request, Returns a channel with one message, either an error (Throwable) or body"
  [url]
  (*pre-http-fn* {:url url})
  (let [res (chan 1)]
    (go (http/get url
                  (fn [http-response]
                    (go (>! res http-response)
                        (close! res)))))
    res))

(defn delete-request
  "Makes DELETE HTTP request, Returns a channel with one message, either an error (Throwable) or body"
  [url]
  (*pre-http-fn* {:url url})
  (let [res (chan 1)]
    (go (http/delete url
                     (fn [http-response]
                       (go (>! res http-response)
                           (close! res)))))
    res))

(defn post-request
  "Makes POST HTTP request, Returns a channel with one message, either an error (Throwable) or body"
  [url & [params]]
  (*pre-http-fn* {:url url :params params})
  (let [res (chan 1)]
    (go (http/post url {:form-params params} (fn [http-response]
                                               (go (>! res http-response)
                                                   (close! res)))))
    res))

(defn error-message
  "the errors are nested so far down in the responses, so so far down"
  [^Throwable e]
  (or (when (instance? clojure.lang.ExceptionInfo e)
        (when (-> e ex-data :body)
          (try (when-let [body (json/read-str (-> e ex-data :body) :key-fn keyword)]
                 (-> body :error :message))
               (catch Throwable e
                 nil))))
      (.getMessage e)))
