(ns chef.food
  (:require [clj-http.client :as client]
            [chef.config :as config]
            [dk.ative.docjure.spreadsheet :as doc]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:use [clojure.java.jdbc]))


; USDA FNDDS 5.0 Data
; Reference: http://www.ars.usda.gov/Services/docs.htm?docid=22370

; DB Definition...

(defn db-with-config [db-config]
  "Build db definition using given configuration."
  {:classname "com.mysql.jdbc.Driver" ; must be in classpath
   :subprotocol "mysql"
   :subname (str "//" (:db-host db-config)
                 ":" (:db-port db-config) 
                 "/" (:db-name db-config))
   :user "chef"
   :password "chef"})

(def FNDDS5-db-config
  {:db-host "localhost"
   :db-port 3306
   :db-name "FNDDS5"})
 
(def FNDDS5-db
  "Definition of the FNDDS5 database. Find out more at
  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22370)."
  (db-with-config FNDDS5-db-config))

; DB Interaction...

(defn query [s db]
  "Run a query with `s` being the SQL query against the database `db`"
  (with-connection db
    (with-query-results rs [s]
      (doall rs))))

;(query "select count(*) from AddFoodDesc" fndds5-db)




; USDA SR25 Data
; Reference: http://www.ars.usda.gov/Services/docs.htm?docid=22771)

(defn usda-SR25-data []
  "Returns the file containing the USDA SR25 2012 abbreviated spreadsheet.
  Find out more at 
  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22771)"
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

(def SR25-db-config
  {:db-host "localhost"
   :db-port 3306
   :db-name "SR25"})

(def SR25-db
  "Definition of the SR25 database. Find out more at 
  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22771)"
  (db-with-config SR25-db-config))


(defn nutritional-value [nutrient-value gram-weight]
  "USDA-specified way to calculating nutritional value.
  `nutrient-value` should be per 100g (Nutr_Val in the [Nutrient Data file](1)),
  `gram-weight` should be (Gm_Wgt in the [Weight file](2))
  [1](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/NUT_DATA.txt)
  [2](http://www.ars.usda.gov/SP2UserFiles/Place/12354500/Data/SR25/asc/WEIGHT.txt)"
  (/ (* nutrient-value gram-weight) 100))




; Other DB Helpers

(defn drop-database [name db]
  (with-connection db
    (with-open [s (.createStatement (connection))]
      (.addBatch s (str "drop database " name))
      (seq (.executeBatch s)))))

(defn create-database [name db]
  (with-connection db
    (with-open [s (.createStatement (connection))]
      (.addBatch s (str "create database " name))
      (seq (.executeBatch s)))))
