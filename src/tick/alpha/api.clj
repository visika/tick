;; Copyright © 2016-2017, JUXT LTD.

(ns tick.alpha.api
  (:refer-clojure :exclude [+ - inc dec max min range time partition-by group-by int long])
  (:require
   [clojure.spec.alpha :as s]
   [tick.core :as core]
   [tick.cal :as cal]
   [tick.interval :as interval])
  (:import
   [java.time Duration ZoneId LocalTime LocalDate]))

;; This API is optimises convenience, API stability and (type) safety
;; over performance. Where performance is critical, use tick.core and
;; friends.

;; clojure.spec assertions are used to check correctness, but these
;; are disabled by default (except when testing).

(defn nanos [n] (core/nanos n))
(defn millis [n] (core/millis n))
(defn seconds [n] (core/seconds n))
(defn minutes [n] (core/minutes n))
(defn hours [n] (core/hours n))
(defn days [n] (core/days n))
(defn weeks [n] (core/weeks n))
(defn now [] (core/now))
(defn today [] (core/today))
(defn tomorrow [] (core/tomorrow))
(defn yesterday [] (core/yesterday))

(defn +
  ([] Duration/ZERO)
  ([arg] arg)
  ([arg & args]
   (reduce #(core/+ %1 %2) arg args)))

(defn - [arg & args]
  (reduce #(core/- %1 %2) arg args))

(defn inc [arg]
  (core/inc arg))

(defn dec [arg]
  (core/dec arg))

(defn max [arg & args]
  (reduce #(core/max %1 %2) arg args))

(defn min [arg & args]
  (reduce #(core/min %1 %2) arg args))

(def range core/range)

(defn int [arg] (core/int arg))
(defn long [arg] (core/long arg))

;; Constructors

(defn date
  ([] (core/date (today)))
  ([v] (core/date v)))
(defn day
  ([] (core/day (today)))
  ([v] (core/day v)))
(defn inst
  ([] (core/inst (now)))
  ([v] (core/inst v)))
(defn instant
  ([] (core/instant (now)))
  ([v] (core/instant v)))
(defn offset-date-time
  ([] (core/offset-date-time (now)))
  ([v] (core/offset-date-time v)))
(defn month
  ([] (core/month (today)))
  ([v] (core/month v)))
(defn year
  ([] (core/year (today)))
  ([v] (core/year v)))
(defn year-month
  ([] (core/year-month (today)))
  ([v] (core/year-month v)))
(defn zone [z] (core/zone z))
(defn zoned-date-time [z] (core/zoned-date-time z))

;; Time

(defn time
  ([] (core/time (now)))
  ([v] (core/time v)))
(defn on [t d] (core/on (time t) (date d)))
(defn at [d t] (core/at (date d) (time t)))
(defn start [v] (core/start v))
(defn end [v] (core/end v))
(defn midnight? [v] (core/midnight? v))

;; Zones

(defn at-zone [t z]
  (core/at-zone t (zone z)))

(defn to-local
  ([v] (core/to-local v))
  ([v z] (core/to-local v (zone z))))

(def UTC (zone "UTC"))
(def LONDON (zone "Europe/London"))

;; Intervals

(defn interval
  "Return an interval which forms the bounding-box of the given arguments."
  ([v] (interval/interval v))
  ([v1 & args] (apply interval/interval v1 args)))

;; An interval is just a vector with at least 2 entries. The 3rd entry
;; onwards are free to use by the caller.
(defn interval? [v] (and (vector? v) (>= (count v) 2)))

(defn duration [interval]
  (let [interval (interval/interval interval)]
    (s/assert :tick.interval/interval interval)
    (interval/duration interval)))

(defn intersection [x y]
  (interval/intersection x y))

;; Useful functions

(defn dates-over [interval]
  (let [interval (interval/interval interval)]
    (s/assert :tick.interval/interval interval)
    (interval/dates-over interval)))

(defn year-months-over [interval]
  (let [interval (interval/interval interval)]
    (s/assert :tick.interval/interval interval)
    (interval/year-months-over interval)))

(defn years-over [interval]
  (let [interval (interval/interval interval)]
    (s/assert :tick.interval/interval interval)
    (interval/years-over interval)))

(defn partition-by [f interval]
  (let [interval (interval/interval interval)]
    (s/assert :tick.interval/interval interval)
    (interval/partition-by f interval)))

(defn partition-by-date [interval]
  (partition-by interval/dates-over interval))

(defn group-by [f interval]
  (let [interval (interval/interval interval)]
    (s/assert :tick.interval/interval interval)
    (interval/group-by f interval)))

(defn group-by-date [interval]
  (group-by interval/dates-over interval))

;; Fixing the clock used for `today` and `now`.

(defmacro with-clock [^java.time.Clock clock & body]
  `(binding [tick.core/*clock* ~clock]
     ~@body))