(ns chef.db
  "Defines chef's interaction with data."
  (:require [clj-http.client :as client]
            [chef.config     :as config]
            [dk.ative.docjure.spreadsheet :as doc]
            [clojure.java.io  :as io]
            [clojure.string   :as s]
            [clojure.java.jdbc :as sql]))


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
           limit 1" SR25-db))
;(example-food-to-group-mapping)

(defn food-to-group
  "Given a food search string, returns top matches with just food description
  and that food's group. Make sure FOOD_DES.Long_Desc has fulltext added; run add-fulltext as shown."
  [food-search-str]
  (query (str "select FOOD_DES.Long_Desc, FD_GROUP.FdGrp_Desc
                 from ABBREV
                 join FOOD_DES on ABBREV.NDB_No = FOOD_DES.NDB_No
                 join FD_GROUP on FOOD_DES.FdGrp_Cd = FD_GROUP.FdGrp_CD
                 where MATCH(FOOD_DES.Long_Desc) AGAINST ('"
                 food-search-str
                 "')") SR25-db))

;(food-to-group "blue cheese")


(defn add-fulltext [db table-name field-name]
  (sql/with-connection db
   (sql/do-commands (str "ALTER TABLE "
                         table-name
                         " "
                         "ADD FULLTEXT("
                         field-name
                         ")"))))
;(add-fulltext SR25-db "FOOD_DES" "Long_Desc")

; Spreadsheet Reading

(defn usda-SR25-data
  "Returns the file containing the USDA SR25 2012 abbreviated spreadsheet.
  Find out more at 
  [this link](http://www.ars.usda.gov/Services/docs.htm?docid=22771)"
  []
  (s/replace (.getPath (io/resource "usda-SR25-abbrev.xlsx"))
             #"%20"
             " "))

(defn read-spreadsheet
  "Read given spreadsheet file."
  [file]
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
