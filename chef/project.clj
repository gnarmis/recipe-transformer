(defproject chef "0.1.0-SNAPSHOT"
  :description "API for food"
  :url "http://github.com/gnarmis/recipe-transformer"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.0"]
                 [clj-http "0.6.4"]
                 [dk.ative/docjure "1.6.0"]
                 [org.clojure/java.jdbc "0.2.3"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [lib-noir "0.4.6"]
                 [compojure "1.1.5"]
                 [ring-server "0.2.8"]
                 [ring/ring-codec "1.0.0"]
                 [org.clojure/data.json "0.2.1"]
                 [inflections "0.8.0"]
                 [bigml/sampling "2.1.0"]
                 [korma "0.3.0-RC4"]
                 [log4j "1.2.15" :exclusions [javax.mail/mail
                                              javax.jms/jms
                                              com.sun.jdmk/jmxtools
                                              com.sun.jmx/jmxri]]]
  :plugins [[lein-ring "0.8.3"]]
  :dev-dependencies [[codox "0.6.4"]]
  :resource-paths ["src/chef/resource"]
  :min-lein-version "2.0.0"
  :ring {:handler chef.food/app})
