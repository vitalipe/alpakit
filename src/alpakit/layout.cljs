(ns alpakit.layout
  (:require
    [clojure.string :refer [join]]
    [garden.selectors :as selectors]

    [alpakit.util :refer [prop-get map-kv]]))


;; util
(defn- map-when [test effect coll]
  (map #(if (test %) (effect %) %)  coll))


(defn areas->css [areas]
  "
     [20%     1fr    2fr    100px
      [:A      :A     :A     :A]  10%
      [:B      :C     :C     nil] 1fr
      [:B      :C     :C     nil] 200px]

     =>

    {:grid-template-areas [[A A A A]
                           [B C C .]
                           [B C C .]]
     :grid-template-rows    10% 1fr 200px
     :grid-template-columns 20%   1fr  2fr 100px
  "
  {:grid-template-areas (->> areas
                          (filter vector?)
                          (mapv (partial map-when nil? (constantly :.))) ;; dot means an empty gird cell
                          (mapv (partial map name))
                          (mapv (partial join " "))
                          (mapv #(str "\"" % "\""))
                          (join " "))

   :grid-template-columns (->> areas
                            (take-while string?)
                            (join " "))
   :grid-template-rows (->> areas
                            (drop-while string?)
                            (drop 1)
                            (take-nth 2)
                            (join " "))})


(defn rows+cols->css [rows cols]
  (merge {}
    (when-not (empty? cols) {:grid-template-columns (join " " cols)})
    (when-not (empty? rows) {:grid-template-rows    (join " " rows)})))


(defn gap->css [gap]
  (let [[row-gap col-gap] (if (string? gap) [gap gap] gap)]
    {:grid-column-gap col-gap
     :grid-row-gap    row-gap}))


(defn place-content->css [[align justify]]
  ;; edge has no support for "place-items", so let's split
  {:align-content (name align)
   :justify-content (name justify)})


(defn place-items->css [[align justify]]
  ;; edge has no support for "place-items", so let's split
  {:align-items (name align)
   :justify-items (name justify)})


(defn auto-sizes->css [[row col]]
  {:grid-auto-columns col
   :grid-auto-rows row})


(defn auto-flow->css [flow]
  {:grid-auto-flow (name flow)})


(defn grid-children->css [index-area-map]
  (->> index-area-map
    ;; I'm not sure if it's a garden bug or just me can't read docs.
    ;; if we pass an int here we get "2n" "1n" etc.. but if we pass a string we just get a number
    (map-kv (comp selectors/nth-child str inc)
            #(hash-map :grid-area (name %)))))




;; layouts
(defprotocol LayoutStrategy
  (generate-layout-styles  [this children]))


(defrecord DefaultLayout []
  LayoutStrategy
  (generate-layout-styles  [_ _] {}))


(defrecord FlexBoxLayout [justify
                          align
                          direction
                          wrap
                          reverse]

  LayoutStrategy
    (generate-layout-styles [{:keys [justify
                                     align
                                     direction
                                     wrap
                                     reverse]} _]

      {:display         "flex"
       :justify-content (name justify)
       :align-items     (name align)
       :flex-wrap       (name wrap)
       :flex-direction  (if reverse
                          (str (name direction) "-reverse")
                          (name direction))}))




(defrecord GridLayout [areas
                       rows
                       cols
                       gap
                       place-items
                       place-content
                       auto-sizes
                       auto-flow]
  LayoutStrategy
    (generate-layout-styles [{:keys [areas
                                     rows
                                     cols
                                     gap
                                     place-items
                                     place-content
                                     auto-sizes
                                     auto-flow]}
                             children]

      (let [ grid-area-names (->> (flatten areas)
                               (filter keyword?)
                               (into #{}))
             index-area-map-via-props (->> children
                                        (map-indexed #(when-let [a (prop-get %2 :grid-area)] {%1 a}))
                                        (into {}))
             index-area-map-via-meta (->> children
                                        (map-indexed #(when-let [a (get (meta %2) :grid-area)] {%1 a}))
                                        (into {}))]

        (into {}
          (remove (comp nil? val)
            (merge {:display "grid"}
                   (areas->css areas)
                   (rows+cols->css rows cols)
                   (gap->css gap)
                   (place-items->css place-items)
                   (place-content->css place-content)
                   (auto-sizes->css auto-sizes)
                   (auto-flow->css auto-flow)
                   (grid-children->css (merge
                                         index-area-map-via-props
                                         index-area-map-via-meta))))))))






;; API

(defn grid [& {:as options}]
  (map->GridLayout (merge {:areas         []
                           :gap           nil
                           :place-items   [:stretch :stretch]
                           :place-content [:stretch :stretch]
                           :auto-sizes    []
                           :rows          []
                           :cols          []
                           :auto-flow     :row}

                          options)))


(defn flex-box [& {:as options}]
  (map->FlexBoxLayout (merge {:justify   :flex-start
                              :align     :stretch
                              :direction :row
                              :wrap      :nowrap
                              :reverse   false}

                          options)))


(defn h-box [& {:as options}]
  (apply flex-box (flatten (into '() (merge options {:direction :row})))))


(defn v-box [& {:as options}]
  (apply flex-box (flatten (into '() (merge options {:direction :column})))))
