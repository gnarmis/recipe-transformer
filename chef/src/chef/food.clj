(ns chef.food
  (:require [chef.config :as config]
            [chef.db :as db]
            [clj-http.client :as client]
            [clojure.string :as s]))


(defn nutritional-value [nutrient-value gram-weight]
  "USDA-specified way to calculating nutritional value.
  `nutrient-value` should be per 100g (Nutr_Val in the [Nutrient Data file](1)),
  `gram-weight` should be (Gm_Wgt in the [Weight file](2))
  [1](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/NUT_DATA.txt)
  [2](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/WEIGHT.txt)"
  (/ (* nutrient-value gram-weight) 100))
