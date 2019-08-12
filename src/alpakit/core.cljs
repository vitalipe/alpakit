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

  :props {-attr       {:default {}                :spec props/html-attr-map}
          -css        {:default {}                :spec props/css-style-map}

          type        {:default :div              :spec keyword?}
          style       {:default {}                :spec map?}
          layout      {:default (layout/->DefaultLayout) :spec (partial satisfies? LayoutStrategy)}}

  (let [style-props    {:style -css
                        :class (->> children
                                 (layout/generate-layout-styles layout)
                                 (merge style)
                                 (css/css!))}]
    (into [type (merge  style-props -attr)] children)))


(defwidget element
  "a building block for dom based widgets"

  :props {-attr       {:default {}                :spec props/html-attr-map}
          -css        {:default {}                :spec props/css-style-map}

          type        {:default :div              :spec keyword?}
          style       {:default {}                :spec map?}}


    (let [props (merge {:style -css :class (css/css! style)} -attr)]
      (into [type props] children)))
