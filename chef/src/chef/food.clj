(ns chef.food
  "Defines the food API, and the overall app handler."
  (:require [chef.config :as config]
            [chef.db :as db]
            [clj-http.client :as client]
            [clojure.string :as s]
            [clojure.data.json :as json]
            [compojure.handler :as handler]
            [compojure.route :as route]
            [noir.util.middleware :as nm]
            [ring.util.codec :as codec])
  (:use [compojure.core]))


(defn wrap-response
  "Helper for wrapping responses for Ring"
  [content status] {:status status
                    :headers {"Content-Type" "application/json"}
                    :body content})

(defn food-search
  "Returns foods matching the name given by param `q`."
  [q]
  (try
    (wrap-response (json/write-str {:results (db/food-search-query (codec/url-decode q))})
                   202)
    (catch Exception e
      (wrap-response (json/write-str {:error "Something went wrong"})
                     409))))

(defn food-group-search
  "Returns foods in given group, sorted ASC by total calories."
  [q]
  (try
    (wrap-response (json/write-str {:results (db/health-candidates-query (codec/url-decode q))})
                   202)
    (catch Exception e
      (wrap-response (json/write-str {:error "Something went wrong"
                                      :exception (str e)})
                     409))))


(def app-routes
  "Vector of forms that define how the routes of the app behave."
  [(GET "/food/:q" [q] (food-search q))
   (GET "/food-group/:q" [q] (food-group-search q))
   (route/not-found "Chef Endpoints: GET /food/:q, GET /food-group/:q")])


(def app
  "Handler for the application, for Ring."
  (nm/app-handler app-routes))



; Blah...
          
(defn nutritional-value
  "USDA-specified way to calculating nutritional value.
  `nutrient-value` should be per 100g (Nutr_Val in the [Nutrient Data file](1)),
  `gram-weight` should be (Gm_Wgt in the [Weight file](2))
  [1](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/NUT_DATA.txt)
  [2](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/WEIGHT.txt)"
  [nutrient-value gram-weight]
  (/ (* nutrient-value gram-weight) 100))
