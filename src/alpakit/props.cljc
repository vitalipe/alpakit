(ns alpakit.props
  (:require
    #?(:cljs [cljs.spec.alpha :as spec])
    #?(:clj  [clojure.spec.alpha :as spec])))


(def html-attr-map   map?)
(def css-style-map   map?)

(def css-size       string?)
(def css-background string?)
(def css-border     string?)

(def on-click-fn    fn?)
