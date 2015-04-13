(ns edn2invoice.rollup)

(require '[clojure.edn :as edn])
(require '[clj-time.core :as t])
(require '[clj-time.coerce :as c])
(require '[clojure.walk :as w])
(require '[clj-time.format :as f])

;;(def data (edn/read-string (slurp "/home/john/MCS/edn2invoice/resources/data/timesheet_2015_04.edn")))

(defn get-data []
  (edn/read-string (slurp "/home/john/MCS/edn2invoice/resources/data/timesheet_2015_04.edn")))

(defn get-client-lookup [data]
  (apply merge
  (flatten
    (map
      (fn [client]
        (map
          (fn [k] {k (key client)})
          (keys (:projects (val client)))))
      (:clients data)))))

(defn chunk-duration [{start :start stop :stop}]
  (t/in-hours (t/interval (c/from-date start) (c/from-date stop))))


(defn is-chunk? [x]
  (and (map? x)
       (contains? x :start)
       (contains? x :stop)))

(defn calculate-durations [m]
  (w/postwalk
    #(if (is-chunk? %)
      (assoc % :duration (chunk-duration %)) (identity %)) m))


(defn merge-projects-daily [s]
  (->> (group-by :project s)
       (map (fn [m] {(key m) (reduce + (map #(:duration %) (val m)))}))))


(defn rollup-month [data]
    (->> (calculate-durations data)
         (:timesheet)
         (map (fn [t] {:day (c/from-date (:day t)) :log (merge-projects-daily (:chunks t))}))))


(defn roll-up-by-client
  ([client] (roll-up-by-client (get-data) client))
  ([data client]
   (let [lookup (get-client-lookup data)
         monthly (rollup-month data)]
     (->>
       (map (fn [day]

              {:day (:day day) :log
                    (apply hash-map (flatten (map #(filter (fn [a] (= client (lookup (key a)))) %) (:log day))))
               }
              ) monthly)
       (filter #(not (empty? (:log %))))))))

(defn project-hours [client]
  (->> (roll-up-by-client client)
       (map #(:log %))
       (apply merge-with +)))

(defn project-rates
  ([] (project-rates (get-data)))
  ([data]
   (->> (for [client (:clients data)
              :let [rate (:rate (val client))]]
          (->> (get-in data [:clients (key client) :projects])
               (map (fn [[k v]] {k (merge {:rate rate} v)}))
               ))
        (flatten)
        (apply merge))))

(defn cost-per-project [client]
  (map (fn [[k v]] (* v (get-in (project-rates (get-data)) [k :rate]))) (project-hours client)))

(defn cost-per-month [client]
  (reduce + (cost-per-project client))
  )


(defn print-date [date]
  (f/unparse (f/formatters :year-month-day) date))


(defn make-invoice-number [date client]
  (let [client-id (get-in (get-data) [:clients client :id])]
    (clojure.string/join [date (format "%02d" client-id)])))


(project-rates)

