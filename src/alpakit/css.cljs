
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



;; loaded styles and css vars, this should reflect the dom state
(def css-registry (atom {}))
(def css-var-registry (atom {}))

;; upcoming changes to be diffed
(def pending-css-updates     (atom {}))
(def pending-css-var-updates (atom {}))

;; cache computed css because it's expensive
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
    (swap! pending-css-updates merge))
  items)


;;; API
(defn reset-css! []
  (reset! css-registry {})
  (reset! css-var-registry {})
  (reset! pending-css-updates {})
  (reset! pending-css-var-updates {}))


(defn css! [styles]
  "register css and return class names"
  (let [[css-vars nomalized-styles] (extract-css-vars styles)]
    (swap! pending-css-var-updates merge css-vars)
    (->> nomalized-styles
      (rules->pairs)
      (create-css-through-cache!)
      (register-css!)
      (map :class-name)
      (string/join " "))))


(defn style-sheet []
  "style container element"
  (let [my-id (random-uuid)

        syncing-css-next-tick (atom false)
        sync-css! (fn [shit]
                    (let [next    @pending-css-updates
                          current @css-registry
                          diff    (reduce (fn [diff [k v]]
                                            (if-not (contains? current k)
                                             (assoc diff k v)
                                             diff))
                                     {} next)]

                      (if (empty? next)
                        ;; at this point we don't really care about removing styles, only handle `reset!`
                        (when (empty? current)
                          (while (pos? (.. shit -cssRules -length)) (.deleteRule shit 0)))
                        ;; when not empty? next
                        (do
                          (reset! pending-css-updates {})
                          (swap! css-registry merge diff)

                          (doall
                            (->> (vals diff)
                              (map :css)
                              (map #(.insertRule shit %))))))))


        syncing-vars-next-tick (atom false)
        sync-css-vars! (fn [shit]
                         (let [next @pending-css-var-updates
                               current @css-var-registry
                               diff (reduce (fn [diff [k v]]
                                              (if (not= v (current k))
                                               (assoc diff k v)
                                               diff))
                                       {} next)]
                           (when-not (empty? next)
                             (swap! css-var-registry merge diff)
                             (reset! pending-css-var-updates {})

                             (doall
                               (map #(.setProperty (.-style shit) (key %) (val %)) diff)))))]


    (r/create-class { :render (fn [] [:style.alpakit-css])
                      :componentWillUnmount #(do
                                               (remove-watch pending-css-var-updates my-id)
                                               (remove-watch pending-css-var-updates my-id))

                      :component-did-mount (fn [this]
                                             (let [css-shit (.-sheet (r/dom-node this))
                                                   css-var-shit (.. js/document -documentElement)]

                                               ;; add all known styles
                                               (sync-css! css-shit)
                                               (sync-css-vars! css-var-shit)


                                               ;; register css var sync
                                               (reset! syncing-vars-next-tick false)
                                               (add-watch
                                                 pending-css-var-updates
                                                 my-id
                                                 (fn [_ _ _ _]
                                                   (when-not @syncing-vars-next-tick
                                                     (reset! syncing-vars-next-tick true)
                                                     (r/next-tick #(do
                                                                     (reset! syncing-vars-next-tick false)
                                                                     (sync-css-vars! css-var-shit))))))

                                               ;; register css sync
                                               (reset! syncing-css-next-tick false)
                                               (add-watch
                                                 pending-css-updates
                                                 my-id
                                                 (fn [_ _ _ _]
                                                   (when-not @syncing-css-next-tick
                                                     (reset! syncing-css-next-tick true)
                                                     (r/next-tick #(do
                                                                     (reset! syncing-css-next-tick false)
                                                                     (sync-css! css-shit))))))))})))
