(ns alpakit.core
  (:require
    [clojure.set :as set]
    [clojure.string :as string]

    [reagent.core :as r]

    [alpakit.widget :refer-macros [defwidget]]
    [alpakit.css    :as css]
    [alpakit.layout :as layout]
    [alpakit.props :as props]))


(defwidget surface
  "a building block for more complex dom based widgets with layout support"

  :props {-attr       {:default {}}

          e           {:default :div}
          css         {:default {}}
          style       {:default {}}

          layout      {:default (layout/->DefaultLayout)}
          transform   {:default nil}}

  (let [transform (or transform (:transform css) {})
        style-props    {:style style
                        :class (->> children
                                 (layout/generate-layout-styles layout)
                                 (merge css transform)
                                 (css/css!))}]
    (into [e (merge  style-props -attr)] children)))


(defwidget element
  "a building block for dom based widgets"

  :props {-attr       {:default {}}

          e           {:default :div}
          css         {:default {}}
          style       {:default {}}

          transform   {:default nil}}


    (let [transform (or transform (:transform css) {})
          props (merge {:style style
                        :class (css/css! (merge css transform))}
                      -attr)]

      (into [e props] children)))
