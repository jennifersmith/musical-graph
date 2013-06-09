(ns musical-graph.core
  (:require [neo4j-batch-inserter.core :refer :all]
            [me.raynes.fs :refer [temp-dir]]
            [clj-http.client :as client]
            [cheshire.core :as json]
            [clj-time.core :as time]
            [clj-time.coerce :as time-coerce]))


(defn get-json [[url params]] 
  (->
   (client/get url params)
   :body
   (json/parse-string true)))

(defn recent-tracks-url [username]
  [
   "http://ws.audioscrobbler.com/2.0/" 
   {:query-params {"method" "user.getrecenttracks"
                   "user" username
                   "api_key" "5166d0f36e63a055d322ea7f99c082a1"
                   "format" "json"
                   "limit" "100"
                   }}])


(defn parse-relationships [{:keys [username] :as user} recent-tracks]
  (let [track-data (get-in recent-tracks [:recenttracks :track])]
    (for [{:keys [url date] :as track} track-data]
      {:from 
       {:id username :type :user} 
       :to 
       {:id url :type "track"} 
       :type "listenedTo" 
       :properties {:date (date :uts)}})))


(defn fetch-data [{:keys [username] :as user}]
  (let [raw-data
        (get-json (recent-tracks-url username))]
    (parse-relationships user raw-data)))


(defn insert-user-tracks [store-dir user]
  (let [relationships (fetch-data user)]
    (insert-batch store-dir 
                  {:auto-indexing {:type-fn :type :id-fn :id}} 
                  {:relationships relationships})))

(defn process-users [& usernames]
  (let [result-dir (.getAbsolutePath (temp-dir "musical-graph"))]
    (println "Saving results to " result-dir)
    (doseq [username usernames]
      (insert-user-tracks result-dir {:username username}))))

(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))


