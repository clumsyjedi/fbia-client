(defproject fbia-client "0.4.7"
  :description "Facebook instant articles client in clojure"
  :url "https://github.com/clumsyjedi/fbia-client"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [http-kit "2.1.18"]
                 [org.clojure/core.async "1.3.610"]
                 [org.clojure/data.json "0.2.6"]
                 [org.apache.httpcomponents/httpclient "4.5.2"]]
  :plugins [[lein-codox "0.10.2"]])
