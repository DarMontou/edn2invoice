(defproject edn2invoice "0.1.0-SNAPSHOT"
  :description "Make Invoices from an EDN Map"
  :min-lein-version "2.0.0"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [compojure "1.3.1"]
                 [ring/ring-defaults "0.1.2"]]
  :plugins [[lein-ring "0.8.13"]]
  :ring {:handler edn2invoice.handler/app
         :auto-reload? true
         :auto-refresh? true}
  :profiles
            {:dev {:dependencies [[javax.servlet/servlet-api "2.5"]
                                  [ring-mock "0.1.5"]
                                  [clj-time "0.9.0"]
                                  [hiccup "1.0.5"]
                                  [ring/ring-jetty-adapter "1.2.1"]
                                  ]}})
