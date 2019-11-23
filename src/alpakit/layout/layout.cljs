(ns alpakit.layout
  (:require
    [alpakit.layout.impl.grid :as grid-layout]
    [alpakit.layout.impl.flexbox :as flex-layout]

    [alpakit.layout.protocol :refer [LayoutStrategy]]))



;; default ccs layout
(def box-layout (reify LayoutStrategy
                  (generate-layout-styles  [_ _] {})))


(defn grid [& {:as options}]
  (grid-layout/map->GridLayout (merge {:areas         []
                                       :gap           nil
                                       :place-items   [:stretch :stretch]
                                       :place-content [:stretch :stretch]
                                       :auto-sizes    []
                                       :rows          []
                                       :cols          []
                                       :auto-flow     :row}

                                      options)))


(defn flex-box [& {:as options}]
  (flex-layout/map->FlexBoxLayout (merge {:justify   :flex-start
                                          :align     :stretch
                                          :direction :row
                                          :wrap      :nowrap
                                          :reverse   false}

                                      options)))


(defn h-box [& {:as options}]
  (apply flex-box (flatten (into '() (merge options {:direction :row})))))


(defn v-box [& {:as options}]
  (apply flex-box (flatten (into '() (merge options {:direction :column})))))
