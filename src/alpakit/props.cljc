(ns alpakit.props)


(defn collect-kv-args [arg-list]
  "takes a seq of keyword args followed by n positional args
   and returns a map and a vector (for positional args).

     for example:

        [:x 42 :y false 1 2 3 4 5] => [{:x 42 :y false} [1 2 3 4 5]]
        [1 2 3 4 5]                => [{} [1 2 3 4 5]]
        [:x 42 :y false]           => [{:x 42 :y false} []]
"
  (let [rest-index (loop [[arg & other] arg-list index 0]
                      (cond
                        (keyword? arg) (recur (rest other) (+ 2 index))
                        :otherwise     index))
        kv-args   (apply hash-map (subvec (into [] arg-list) 0 rest-index))
        rest-list (subvec (into [] arg-list) rest-index (count arg-list))]

      [kv-args rest-list]))


(defn get-prop
  ([e key] (get-prop e key nil))
  ([[element & ?props] key default]
   (cond
     (map? (first ?props)) (get (first ?props) key default)
     (keyword?  element)   default
     ;; FIXME: this is not fast!
     :otherwise             (get-in (collect-kv-args ?props) [0 key] default))))


(defn get-key [e]
  (or ;; prefer prop over meta..
    (get-prop  e   :key)
    (get (meta e)  :key)))


(defn args->props
  "collect kv or hiccup into a map with default values, useful for props+children"
  ([argv] (args->props argv {}))
  ([argv defaults]
   (let [[props children] (collect-kv-args argv)]
     (merge defaults props {:children (apply list children)}))))
