
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
(defn hashable->css-str [thing]
  (.toString (hash thing) 36)) ;; 36 = [a-z] + [0-10]


(defn extract-css-vars [styles]
  "extracts atoms and replaces them with a css var in the form --alpakit-var-{{(hashable->css-str da-atom)}}
   returns a vector with the replaced var map and the normalized styles
  "
  (let [css-vars   (atom {})
        normalized (->> styles
                     (clojure.walk/postwalk
                       (fn [form]
                         (if (satisfies? IDeref form)
                           (let [id  (str "--alpakit-css-var-" (hashable->css-str form))]
                             (swap! css-vars assoc id (deref form))
                             (str "var(" id ")"))
                          ;; otherwise don't fuck with it
                          form))))]

       [@css-vars normalized]))

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
   (str "alpakit-" (hashable->css-str coll)))


(defn- ->style [selector rules]
  (let [class-name (->class-name [selector rules])]
    {:css          (css [(str "." class-name) [selector rules]])
     :style        [selector rules]
     :class-name   class-name}))



;; watch this for changes and update the dom. this should reflect the dom state
(def registry (atom {}))

;; current css var values
(def css-var-registry (atom {}))

;; upcoming changes to css vars
(def css-var-updates (atom {}))

;; cache computed css
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
  (let [[css-vars nomalized-styles] (extract-css-vars styles)]
    (swap! css-var-updates merge css-vars)
    (->> nomalized-styles
      (rules->pairs)
      (create-css-through-cache!)
      (register-css!)
      (map :class-name)
      (string/join " "))))


(defn style-sheet []
  "style container element"
  (let [my-id (random-uuid)
        css-var-shit (.. js/document -documentElement)
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
                                              (map #(.insertRule sheet %)))))))

        syncing-vars-next-tick (atom false)
        sync-css-vars! (fn [shit]
                         (let [next @css-var-updates
                               current @css-var-registry
                               diff (reduce (fn [diff [k v]]
                                              (if (not= v (current k))
                                               (assoc diff k v)
                                               diff))
                                       {} next)]

                           (swap! css-var-registry merge diff)
                           (reset! css-var-updates {})
                           (doall (map #(.setProperty (.-style shit) (key %) (val %)) diff))))]

    (r/create-class { :render (fn [] [:style.alpakit-css])
                      :componentWillUnmount #(remove-watch registry my-id)
                      :component-did-mount (fn [this]
                                             (let [css-shit (.-sheet (r/dom-node this))]

                                               ;; add all known styles
                                               (doall
                                                 (->> (vals @registry)
                                                   (map :css)
                                                   (map #(.insertRule css-shit %))))

                                               ;; register css var sync
                                               (reset! syncing-vars-next-tick false)
                                               (add-watch
                                                 css-var-updates
                                                 my-id
                                                 (fn [_ _ _ _]
                                                   (when-not @syncing-vars-next-tick
                                                     (reset! syncing-vars-next-tick true)
                                                     (r/next-tick #(do
                                                                     (reset! syncing-vars-next-tick false)
                                                                     (sync-css-vars! css-var-shit))))))

                                               ;; register sync
                                               (add-watch
                                                 registry
                                                 my-id
                                                 (fn [_ _ prv next]
                                                   (r/next-tick #(sync-styles! prv next css-shit))))))})))
