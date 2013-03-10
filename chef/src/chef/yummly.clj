(ns chef.yummly
  (:require [clj-http.client :as client]
            [chef.config :as config]))


(defn yummly-api [auth]
  {:endpoint 	"http://api.yummly.com/v1"
   :resources {:recipe-search "/api/recipes"}
   :params 		["q", "requirePictures", "allowedIngredient[]", "excludedIngredient[]",
               "allowedDiet[]", "allowedAllergy[]", "allowedCuisine[]", 
               "allowedCourse[]", "allowedHoliday[]", "excludedCuisine[]", 
               "excludedCourse[]", "excludedHoliday[]", "maxTotalTimeInSeconds",
               "nutrition.ATTR.{min|max}", "maxResult", "start",
               "flavor.ATTR.{min|max}", "facetField[]"]
   :auth 			{"_app_id" (:app-id auth),
               "_app_key" (:app-key auth)}
   :schemas 	{:nutrition {"K" "Potassium",
                           "NA" "Sodium",
                           "CHOLE" "Cholestrol",
                           "FATRN" "Fatty acids, total trans",
                           "FASAT" "Fatty acides, total saturated",
                           "CHOCDF" "Carbohydrate, by difference",
                           "FIBTG" "Fiber, total dietary",
                           "PROCNT" "Protein",
                           "VITC" "Vitamin C",
                           "CA" "Calcium",
                           "FE" "Iron",
                           "SUGAR" "Sugars, total",
                           "ENERC_KCAL" "Energy",
                           "FAT" "Total lipid",
                           "VITA_IU" "Vitamin A, IU"}
               :flavor ["sweet", "meaty", "sour", "bitter", "sweet", "piquant"]}})


(defn yummly-search [url-params api]
  "Search yummly"
  (let [req (str (:endpoint api)
                 (-> api :resources :recipe-search)
                 "?"
                 (->> (:auth api)
                      vec
                      (map #(clojure.string/join "=" %))
                      (clojure.string/join "&"))
                 url-params)]
    (println (str "Query: " req))
    (try (client/get req)
      (catch Exception e (println (str "Error caught: " e))))))
      
; Example search:
; (yummly-search "&q=beef" (yummly-api (config/app-keys :yummly)))