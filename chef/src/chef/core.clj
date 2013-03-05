(ns chef.core
 (:require [chef.config :as config]
           [clj-http.client :as client]))



(def recipe {:ingredients [], :steps []})

(def ingreds-eg ["mushrooms", "flour", "ground beef"])

(def steps-eg ["", ""])

(config/app-keys :yummly)

(keys (client/get "http://google.com"))

(def yummly-api {:endpoint 	"http://api.yummly.com/v1"
                 :params 		["q", "requirePictures", "allowedIngredient[]", "excludedIngredient[]",
                          	 "allowedDiet[]", "allowedAllergy[]", "allowedCuisine[]", 
                          	 "allowedCourse[]", "allowedHoliday[]", "excludedCuisine[]", 
                          	 "excludedCourse[]", "excludedHoliday[]", "maxTotalTimeInSeconds",
                          	 "nutrition.ATTR.{min|max}", "maxResult", "start",
                             "flavor.ATTR.{min|max}", "facetField[]"]
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


;(defn -main
;  "I don't do a whole lot."
;  [& args]
;  (println "Hello, World!"))
