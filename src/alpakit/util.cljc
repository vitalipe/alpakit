(ns alpakit.util)


(defn map-kv [k-fn v-fn a-map]
  "map over a seq of [k v] pairs, returns a map"
  (->> a-map
    (map #(vector (k-fn (first %)) (v-fn (second %))))
    (into {})))


(defn map-keys [k-fn a-map]
  "map over keys"
  (map-kv k-fn identity a-map))


(defn map-vals [v-fn a-map]
  "map over vals"
  (map-kv identity v-fn a-map))


(defn pivot-by [f items]
  "like group by, but will only keep the first val"
  (->> items
    (group-by f)
    (map-vals first)))


(defn collect-kv-args [arg-list]
  "takes a seq of keyword args followed by n positional args
   and returns a map and a vector (positional args).

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


(defn prop-get [[element & ?props] key]
  (cond
    (map? (first ?props)) (get (first ?props) key)
    (keyword?  element)    nil
    :otherwise             (get-in (collect-kv-args ?props) [0 key])))

(defn key-get [e]
  (or ;; prefer prop over meta..
        (prop-get  e   :key) ;; FIXME: this is not fast!
        (get (meta e)  :key)))
