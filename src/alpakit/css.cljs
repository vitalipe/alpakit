
(ns alpakit.css
  "a simple clj implementation of styled components."
  (:require
    [clojure.set    :as set]
    [clojure.string :as string]

    [cljs.cache :as cache]
    [garden.core :refer [css]]
    [reagent.core :as r]
    [alpakit.util :refer [pivot-by]]))


;; helpers
(defn rules->pairs [rules]
  "takes a rule map and returns a list of [selector rule] pairs.
   assumes that each map val is a rule and it's key is a selector"
  (when-not (empty? rules)
    (let [selector-keys (->> rules
                          (filter (comp map? val))
                          (map first))]
       (into
         [[:& (apply dissoc rules selector-keys)]]
         (map #(vector % (rules %)) selector-keys)))))


(defn- ->class-name [coll]
  "use hash to make unique short class names"
   (str "alpakit-" (.toString (hash coll) 36))) ;; radix 36 = (a-z + 0-10)


(defn- ->style [selector rules]
  (let [class-name (->class-name [selector rules])]
    {:css          (css [(str "." class-name) [selector rules]])
     :style        [selector rules]
     :class-name   class-name}))


;; cache
(def registry (atom {}))
(def style-cache (atom (cache/lru-cache-factory {} :threshold 10000)))


(defn create-css-through-cache! [rule-pairs]
  (doall
    (->> rule-pairs
      (map #(let [in-cache (cache/has? @style-cache %)
                  style (if in-cache
                          (cache/lookup @style-cache %)
                          (apply ->style %))]
              (if in-cache
                  (swap! style-cache cache/hit %)
                  (swap! style-cache cache/miss % style))

              style)))))


(defn register-css! [items]
  (->> items
    (pivot-by :class-name)
    (swap! registry merge))
  items)


;;; API
(defn reset-css! []
  (reset! registry {}))


(defn css! [styles]
  "register css and return class names"
  (->> styles
    (rules->pairs)
    (create-css-through-cache!)
    (register-css!)
    (map :class-name)
    (string/join " ")))


(defn style-sheet []
  "style container element"
  (let [my-id (random-uuid)
        lock  (atom 0)
        sync-styles! (fn [prv next sheet]
                      (cond
                        ;; at this point we don't really care about removing styles, only handle `reset!`
                        (empty? next) (while (pos? (.. sheet -cssRules -length)) (.deleteRule sheet 0))
                        :otherwise    (let [new-rules (select-keys
                                                        next
                                                        (set/difference
                                                          (into #{} (keys next))
                                                          (into #{} (keys prv))))]
                                          (doall
                                            (->> (vals new-rules)
                                              (map :css)
                                              (map #(.insertRule sheet %)))))))]

    (r/create-class { :render (fn [] [:style.alpakit-css])
                      :componentWillUnmount #(remove-watch registry my-id)
                      :component-did-mount (fn [this]
                                             (let [css-shit (.-sheet (r/dom-node this))]
                                               ;; add all known styles
                                               (doall
                                                 (->> (vals @registry)
                                                   (map :css)
                                                   (map #(.insertRule css-shit %))))
                                               ;; register sync
                                               (add-watch
                                                 registry
                                                 my-id
                                                 (fn [_ _ prv next]
                                                   (let [lock-id (swap! lock inc)]
                                                     (r/next-tick #(when (= lock-id @lock)
                                                                     (sync-styles! prv next css-shit))))))))})))
