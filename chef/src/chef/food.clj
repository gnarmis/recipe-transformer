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

(defn food-search
  "A function that returns a JSON response after doing a search
  using the parameter `q`"
  [q]
  (try
    {:status  200
     :headers {"Content-Type" "application/json"}
     :body    (json/write-str {:results (db/food-to-group (codec/url-decode q))})}
    (catch Exception e
      {:status  409
       :headers {"Content-Type" "application/json"}
       :body    (json/write-str {:error "Something went wrong"})})))

(def app-routes
  "Vector of forms that define how the routes of the app behave."
  [(GET "/food/search/:q" [q] (food-search q))
   (route/not-found "Chef.food")])


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
