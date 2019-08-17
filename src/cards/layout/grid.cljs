(ns cards.layout.grid
  (:require
    [devcards.core :refer-macros [defcard-rg]]

    [garden.units   :refer [px]]
    [alpakit.core   :refer [surface]]
    [alpakit.layout :refer [grid]]))


(defn box [color text]
  [:div {:style {:background-color color}} text])


(defcard-rg simple-grid-layout
  "
    to define nice looking grid layouts, simply add named `:areas` and provide `:rows` and `:cols`
  "
  [surface :layout (grid :areas [[:A  :A  :C]
                                 [:B  :B  :C]]
                         :cols ["40px" "1fr" "1fr"]
                         :rows ["60px" "40px"])

   ^{:grid-area :A}
   [box "#0086b3" "A"]

   ^{:grid-area :B}
   [box "gold" "B"]

   ^{:grid-area :C}
   [box "pink" "C"]])

(defcard-rg simple-inline-grid-layout
  "
    it's also possible to define `:rows` and/or `:cols` inside `:areas`
  ```
  :areas [\"40px\" \"1fr\" \"1fr\"
          [:A    :A    :C] (px 60)
          [:B    :B    :C] \"40px\"]
  ```

  "
   [surface :layout (grid :areas ["40px" "1fr" "1fr"
                                  [:A    :A    :C] (px 60)
                                  [:B    :B    :C] "40px"])


    ^{:grid-area :A}
    [box "#0086b3" "A"]

    ^{:grid-area :B}
    [box "gold" "B"]

    ^{:grid-area :C}
    [box "pink" "C"]])


(defcard-rg header-menu-footer-layout
  "
  ```
  :areas [\"1fr\"     \"1fr\"      \"1fr\"
          [:header  :header   :header ] \"40px\"
          [:menu    :content  :content] \"200px\"
          [:menu    :content  :content] \"200px\"
          [:footer  :footer   :footer ] \"40px\"]
  ```

  "
  [surface :layout (grid :areas ["1fr"     "1fr"      "1fr"
                                 [:header  :header   :header ] "40px"
                                 [:menu    :content  :content] "200px"
                                 [:menu    :content  :content] "200px"
                                 [:footer  :footer   :footer ] "40px"])

   ^{:grid-area :header}
    [box "#0086b3" "header"]

    ^{:grid-area :content}
    [box "gold" "content"]

    ^{:grid-area :menu}
    [box "pink" "menu"]

    ^{:grid-area :footer}
    [box "brown" "footer"]])


(defcard-rg grid-layout-with-holes
  "
  set areas to `nil` to make great art!
  ```
  :areas [\"1fr\"     \"1fr\"      \"1fr\"
          [:header  :header   :header ] \"40px\"
          [:menu      nil       nil   ] \"200px\"
          [nil        nil     :content] \"200px\"
          [nil      :footer     nil   ] \"40px\"]

  ```

  "
  [surface :layout (grid :areas ["1fr"     "1fr"      "1fr"
                                 [:header  :header   :header ] "40px"
                                 [:menu      nil       nil   ] "200px"
                                 [nil        nil     :content] "200px"
                                 [nil      :footer     nil   ] "40px"])

   ^{:grid-area :header}
    [box "#0086b3" "header"]

    ^{:grid-area :content}
    [box "gold" "content"]

    ^{:grid-area :menu}
    [box "pink" "menu"]

    ^{:grid-area :footer}
    [box "brown" "footer"]])


(defcard-rg grid-layout-with-gaps
  "
  to add gaps between rows and cols use the `:gap` prop
  ```
  :gap [\"20px\" \"50px\"]
  ```
  "
  [surface :layout (grid  :gap ["20px" "50px"]
                          :areas ["1fr"     "1fr"      "1fr"
                                  [:header  :header   :header ] "40px"
                                  [:c0      :c1        :c2    ] "200px"
                                  [:footer  :footer   :footer ] "40px"])

   ^{:grid-area :header}
    [box "#0086b3" "header"]

    ^{:grid-area :c0}
    [box "gold" "C0"]

    ^{:grid-area :c1}
    [box "pink" "C1"]

    ^{:grid-area :c2}
    [box "pink" "C2"]

    ^{:grid-area :footer}
    [box "brown" "footer"]])

(defcard-rg layout-with-autoflow
  "
  children without an area key are inserted automatically.

  see `:auto-sizes` and `:auto-flow` for more options.

  ```
   :areas [\"1fr\"     \"1fr\"      \"1fr\"
           [:A        :B         :C ] \"40px\"
           [:D        :E         :F ] \"60px\"]
    :B [box ...]

    [box \"gold\"  ...
    [box \"pink\"  ...]
    [box \"grey\"  ...]
    [box \"brown\" ...]])

  ```
  "
  [surface :layout (grid :areas ["1fr"     "1fr"      "1fr"
                                 [:A        :B         :C ] "40px"
                                 [:D        :E         :F ] "60px"])

   ^{:grid-area :B}
    [box "#0086b3"    "B"]

    [box "gold"    "A (child 1 auto fill)"]
    [box "pink"    "C (child 2 auto fill)"]
    [box "grey"    "D (child 3 auto fill)"]
    [box "brown"   "E (child 4 auto fill)"]])
