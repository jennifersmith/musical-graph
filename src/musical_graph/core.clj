(ns musical-graph.core
  (:require [neo4j-batch-inserter.core :refer :all])
  (:require [clj-http.client :as client])
  (:require [cheshire.core :as json]))


(def get-json (comp 
               #(json/parse-string % true) 
               :body 
               client/get))

(defn recent-tracks-url [username]
  (format
   "http://ws.audioscrobbler.com/2.0/?method=user.getrecenttracks&user=%s&api_key=5166d0f36e63a055d322ea7f99c082a1&format=json" username))


(defn parse-relationships [user recent-tracks]
  (let [track-data (get-in recent-tracks [:recenttracks :track])]
    (for [track track-data]
      (track :name))))


(defn fetch-data [{:keys [username] :as user}]
  (let [raw-data
        (get-json (recent-tracks-url username))]
    (parse-relationships user raw-data)))


(defn -main
  "I don't do a whole lot."
  [& args]
  (println "Hello, World!"))


