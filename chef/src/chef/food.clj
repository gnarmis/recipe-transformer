(ns chef.food
  (:require [clj-http.client :as client]
            [chef.config :as config]
            [dk.ative.docjure.spreadsheet :as doc]
            [clojure.java.io :as io]
            [clojure.string :as s]))


(defn usda-SR25-data []
  "Returns the file containing the USDA SR25 2012 abbreviated spreadsheet.
  Find out more at [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22771)"
  (s/replace (.getPath (io/resource "usda-SR25-abbrev.xlsx"))
             #"%20"
             " "))


(defn read-spreadsheet [file]
  "Read given spreadsheet file."
  (->> (doc/load-workbook file)
       (doc/select-sheet "Abbrev")
       (doc/select-columns {:B :food-description
                            :C :water
                            :D :energy
                            :E :protein
                            :F :lipid
                            :H :carbohydrate
                            :I :fiber
                            :J :sugar
                            :AS :saturated-fatty-acid
                            :AT :monounsaturated-fatty-acid
                            :AU :polyunsaturated-fatty-acid
                            :AV :cholestrol
                            :AW :gram-weight-1
                            :AX :gram-weight-1-description
                            :AY :gram-weight-2
                            :AZ :gram-weight-2-description})))


;(second (read-spreadsheet (usda-SR25-data)))


(defn nutritional-value [nutrient-value gram-weight]
  "USDA-specified way to calculating nutritional value.
  `nutrient-value` should be per 100g (Nutr_Val in the [Nutrient Data file](1)),
  `gram-weight` should be (Gm_Wgt in the [Weight file](2))
  [1](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/NUT_DATA.txt)
  [2](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/WEIGHT.txt)"
  (/ (* nutrient-value gram-weight) 100))





