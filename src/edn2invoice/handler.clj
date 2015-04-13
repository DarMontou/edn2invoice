(ns edn2invoice.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [edn2invoice.rollup]
            [hiccup.core :refer :all]
            [hiccup.page :refer :all]
            [ring.adapter.jetty :refer :all]
            ))

(defn template [body]
  (html5
    [:head  (include-css "/style.css")]
    [:body body]))

(defn to-dollar [n]
  (format "$%,.2f" (float n)))

(defn print-hours [n]
  (format "%.2f" (float n)))

(defn print-header[client]
  (html5
    [:header
     [:div {:id "logo"} "INVOICE"]
     [:div {:id "MCS"} "Madison Consulting Services"]

     [:address
       [:p   "John Madison"]
       [:p   "5000 Wing Rd."
       [:br] "Austin, TX 78749"]
       [:p   "john@madison-consulting-services.com"]
       [:p   "(512) 745-7618"]]]))

(defn wrap [client]
  (template
    (html

      (print-header client)

      [:table
       [:tr [:th "DATE"] [:th "PROJECT"] [:th "HOURS"] [:th "RATE"] [:th "TOTAL"]]
       (for [day (edn2invoice.rollup/roll-up-by-client (keyword client))
             project (:log day)]
         (let [pid (key project)
               hours (val project)
               rate  (get-in (edn2invoice.rollup/project-rates) [pid :rate])
               total (* rate hours)]

           [:tr [:td (edn2invoice.rollup/print-date (:day day))]
            [:td pid]
            [:td {:align "right"} (print-hours hours)]
            [:td {:align "right"} (to-dollar rate)]
            [:td {:align "right"} (to-dollar total)]]))
       [:tr [:th {:align "right" :colspan "4"} "TOTAL"] [:th {:align "right"}(to-dollar (edn2invoice.rollup/cost-per-month (keyword client)))]]

       ])))


(defroutes app-routes
  (GET "/" [] (template "Madison Consulting Services Invoicing System"))
  (GET "/client/:client" [client] (wrap client))
  (route/resources "/")
  (route/not-found "Not Found"))

(def app
  (wrap-defaults app-routes site-defaults))


