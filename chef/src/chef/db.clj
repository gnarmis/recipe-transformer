(ns chef.db
  "Defines chef's interaction with data."
  (:require [clj-http.client :as client]
            [chef.config     :as config]
            [dk.ative.docjure.spreadsheet :as doc]
            [clojure.java.io  :as io]
            [clojure.string   :as s]
            [clojure.java.jdbc :as sql]
            (bigml.sampling [simple :as simple]
                            [reservoir :as reservoir]
                            [stream :as stream]))
  (:use [inflections.core]))


; DB Definitions...

(defn db-with-config 
  "Build db definition using given configuration."
  [db-config]
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

(def SR25-db-config
  {:db-host "localhost"
   :db-port 3306
   :db-name "SR25"}) 
 
(def FNDDS5-db
  "Definition of the FNDDS5 database. Find out more at
  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22370)."
  (db-with-config FNDDS5-db-config))

(def SR25-db
  "Definition of the SR25 database. Find out more at 
  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22771)"
  (db-with-config SR25-db-config))


; some useful functions...

(defn query
  "Run a query with `s` being the SQL query against the database `db`"
  [s db]
  (sql/with-connection db
    (sql/with-query-results rs [s]
      (doall rs))))
;(query "select count(*) from AddFoodDesc" FNDDS5-db)

(defn example-food-to-group-mapping
  "This select statement produces tuples that contain both the food description
  and the food group."
  []
  (query "select ABBREV.NDB_No, ABBREV.Shrt_Desc, FOOD_DES.NDB_No, FOOD_DES.FdGrp_Cd, FD_GROUP.FdGrp_CD, FD_GROUP.FdGrp_Desc
           from ABBREV
           join FOOD_DES on ABBREV.NDB_No = FOOD_DES.NDB_No
           join FD_GROUP on FOOD_DES.FdGrp_Cd = FD_GROUP.FdGrp_CD
           limit 1"
         SR25-db))
;(example-food-to-group-mapping)

(declare process-food-query)

(defn food-search-query
  "Given a food search string, returns top matches with just food description
  and that food's group. Make sure FOOD_DES.Long_Desc has fulltext added; run add-fulltext as shown."
  [food-search-str]
  (->> (query (str "select *
                   from ABBREV
                   join FOOD_DES on ABBREV.NDB_No = FOOD_DES.NDB_No
                   join FD_GROUP on FOOD_DES.FdGrp_Cd = FD_GROUP.FdGrp_CD
                   where MATCH(FOOD_DES.Long_Desc) AGAINST ('"
                   food-search-str
                   "')")
              SR25-db)
       (process-food-query)))

(defn process-food-query
  "Organize and clean food query."
  [results]
  (for [r results]
    {:name (:long_desc r)
     :group (:fdgrp_desc r)
     :NDB_No (:ndb_no r)
     :nutrition {(keyword "energy_(kcal)") ((keyword "energ_kcal") r)
                 (keyword "carbohydrate_(g)") ((keyword "carbohydrt_(g)") r)
                 (keyword "protein_(g)") ((keyword "protein_(g)") r)
                 (keyword "lipid_(g)") ((keyword "lipid_tot_(g)") r)
                 (keyword "sugar_(g)") ((keyword "sugar_tot_(g)") r)
                 (keyword "fiber_(g)") ((keyword "fiber_td_(g)") r)
                 (keyword "cholestrol_(mg)") ((keyword "cholestrl_(mg)") r)
                 (keyword "water_(g)") ((keyword "water_(g)") r)
                 (keyword "total-weight_(g)") (apply + (map #(if (not= nil %) (int %) 0)
                                                            [((keyword "gmwt_1") r)
                                                             ((keyword "gmwt_2") r)]))
                 :quantity [{:quantity-type ((keyword "gmwt_desc1") r)
                             (keyword "quantity-weight_(g)") ((keyword "gmwt_1") r)}
                            {:quantity-type ((keyword "gmwt_desc2") r)
                             (keyword "quantity-weight_(g)") ((keyword "gmwt_2") r)}]
                 }}))

;(-> (food-search-query "bacon") first)


(defn health-candidates-query
  "Given a food group string, return list of member foods, sorted ASC by number of calories"
  [q]
  (->> (query (str "select *
              from ABBREV
              join FOOD_DES on ABBREV.NDB_No = FOOD_DES.NDB_No
              join FD_GROUP on FOOD_DES.FdGrp_Cd = FD_GROUP.FdGrp_CD
              where MATCH(FD_GROUP.FdGrp_Desc) AGAINST ('"
              q
              "')
              order by ABBREV.`Energ_Kcal` ASC
              limit 10")
              SR25-db)
       process-food-query))


(defn try-query-with-inflections
  "Try plural or singular. Expects plural."
  [ingred-name]
  (let [q (food-search-query ingred-name)]
    (cond
     (= (count q) 0) (-> ingred-name singular food-search-query)
     :else q)))


(defn upgrade-ingredients
  "Search and add extra info for each ingredient."
  [ingredients]
  ;(-> ingredients first :name singular food-search-query println)
  ;(-> (try-query-with-inflections (:name (first ingredients))) first println)
  (for [ingred ingredients]
    (let [result (-> (try-query-with-inflections (:name ingred))
                     first)]
      (-> ingred
          (assoc :best-match-NDB_No (:NDB_No result))
          (assoc :best-match-description (:name result))
          (assoc :best-match-group (:group result))
          (assoc :nutrition (:nutrition result))))))


(defn replace-ingredient
  "replaces ingredient"
  [ingreds string-to-replace]
  (first (take 1 (simple/sample ingreds))))

(defn replace-ingredients
  "Replace specific ingredients as they appear."
  [steps ingreds]
  (for [step steps]
    (let [replacement (->> (re-seq #"%[A-Z0-9]+%" (:step step)) 
                           first 
                           (replace-ingredient ingreds))]
      (assoc 
        step
        :step 
        (s/replace (:step step) #"%[A-Z0-9]+%" (:name replacement))))))



(defn transform-query
  "Transform recipe by replacing some ingredients"
  [recipe]
  ;(println (-> recipe :ingredients first :name))
  (let [upgraded-ingreds (upgrade-ingredients (:ingredients recipe))]
    {:ingredients upgraded-ingreds
     :steps (replace-ingredients (:steps recipe) upgraded-ingreds)}))






(defn add-fulltext
  "Add fulltext search using ALTER TABLE SQL queries."
  [db table-name field-name]
  (sql/with-connection db
   (sql/do-commands (str "ALTER TABLE "
                         table-name
                         " "
                         "ADD FULLTEXT("
                         field-name
                         ")"))))

(defn prepare-SR25-db
  "Makes sure the DB has the right alterations done to it."
  []
  (add-fulltext SR25-db "FOOD_DES" "Long_Desc")
  (add-fulltext SR25-db "FD_GROUP" "FdGrp_Desc"))


; Spreadsheet Reading

;(defn usda-SR25-data
;  "Returns the file containing the USDA SR25 2012 abbreviated spreadsheet.
;  Find out more at 
;  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22771)"
;  []
;  (s/replace (.getPath (io/resource "usda-SR25-abbrev.xlsx"))
;             #"%20"
;             " "))

;(defn read-spreadsheet
;  "Read given spreadsheet file."
;  [file]
;  (->> (doc/load-workbook file)
;       (doc/select-sheet "Abbrev")
;       (doc/select-columns {:B :food-description
;                            :C :water
;                            :D :energy
;                            :E :protein
;                            :F :lipid
;                            :H :carbohydrate
;                            :I :fiber
;                            :J :sugar
;                            :AS :saturated-fatty-acid
;                            :AT :monounsaturated-fatty-acid
;                            :AU :polyunsaturated-fatty-acid
;                            :AV :cholestrol
;                            :AW :gram-weight-1
;                            :AX :gram-weight-1-description
;                            :AY :gram-weight-2
;                            :AZ :gram-weight-2-description})))
;(second (read-spreadsheet (usda-SR25-data)))

; Other DB Helpers

(defn drop-database [name db]
  (sql/with-connection db
    (with-open [s (.createStatement (sql/connection))]
      (.addBatch s (str "drop database " name))
      (seq (.executeBatch s)))))

(defn create-database [name db]
  (sql/with-connection db
    (with-open [s (.createStatement (sql/connection))]
      (.addBatch s (str "create database " name))
      (seq (.executeBatch s)))))
