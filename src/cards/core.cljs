(ns cards.core
  (:require
    [reagent.core :as r]
    [devcards.core]

    [alpakit.core   :refer [surface element]]
    [alpakit.widget :refer-macros [defwidget]]
    [alpakit.css :refer [css! style-sheet]]
    [alpakit.layout :refer [grid]]
    [alpakit.animation :refer [spring+control] :as animation]

    ;; cards
    ;[cards.widget]
    ;[cards.layout.grid]
    ;[cards.layout.flex]))

    [devcards.core :refer-macros [defcard-rg]]))

(enable-console-print!)

(defn main []) ;; noop


(defwidget demo

  [:div
   [style-sheet]

   [surface
    :layout (grid :areas [ "100px"  "1fr"   "200px"
                          [:l-menu :header  :header] "100px"
                          [:l-menu :content :r-menu] "1fr"
                          [:footer :footer  :footer] "100px"])

     ^{:grid-area :footer}
      [surface [:h1 "footer"]]

     ^{:grid-area :header}
      [:h1  "header"]

     ^{:grid-area :l-menu}
      [:h1  "left"]

     ^{:grid-area :r-menu}
      [:h1  "right"]

     ^{:grid-area :content}
      [surface "WATTTT"]]



   [surface :style {:background-color "red"
                    :border-color "blue"
                    :&:hover {:background-color "yellow"}}
     [:h1 "works!"]]

   (comment
      [surface
          :layout (grid :areas ["100px" "1fr" "200px"
                                [:header :header  :header] "100px"
                                [:l-menu :content :r-menu] "1fr"
                                [:footer :footer  :footer] "100px"])

          [:h1 {:grid-area :header} "header"]
          [:h1 {:grid-area :header} "content"]
          [:h1 {:grid-area :header} "left menu"]
          [:h1 {:grid-area :header} "right menu"]])])


(defn anim-demo []
  (r/with-let [[s cfg]   (spring+control :from 0 :to 1 :stiffness 1 :velocity 0)
               [s1 cfg1] (spring+control :from 0 :to 100 :stiffness 1 :velocity 0)]
    [:div
     [:div "value: " @s]
     [:button {:on-click #(swap! cfg update :to - 0.2)} "-"]
     [:button {:on-click #(swap! cfg update :to + 0.2)} "+"]

     [:div {:style {:background-color "yellow"
                    :display "inline"
                    :padding "10px"
                    :width (str (* 100 @s) "px")
                    :position "absolute"}}
        "wat"]
     [:div "?"]

     [:div "value: " @s1]
     [:button {:on-click #(swap! s1  - 20)} "-"]
     [:button {:on-click #(swap! s1  + 20)} "+"]

     [:div {:style {:background-color "yellow"
                    :display "inline"
                    :padding "10px"
                    :width (str @s1 "px")
                    :position "absolute"}}
        "wat"]
     [:div "?"]
     [:button {:on-click #(do
                            (swap! cfg  update :to + 0.2)
                            (swap! cfg1 update :to + 20))}
          "++"]


     [:div (str @cfg)]
     [:div (str @cfg1)]]))


(defn with-interval! [timeMs anim f]
  (js/setInterval #(swap! anim f) timeMs)
  anim)


(defn anim-demo-logo []
  (r/with-let [[s cfg]   (spring+control)
               spin      (with-interval! 1000 (animation/spring :to 30 :damping 0.7) #(if (= 30 %) 0 30))]
    [:div
     [:div "s: " @s]
     [:div "spin: " @spin]

     [:div {:style {:background-color "yellow"
                    :display "inline"
                    :padding "10px"
                    :width (str (+ 100 @s) "px")
                    :position "absolute"}}
        "s"]
     [:div "?"]
     [:div "?"]
     [:div {:style {:background-color "yellow"
                    :display "inline"
                    :padding "10px"
                    :width (str (+ 100 @spin) "px")
                    :position "absolute"}}
        "spin"]
     [:div "?"]

     [:img
      {:src "http://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
       :style {:transform (str "scale(" (min 5 (max 0.1 (* 0.01 @s))) ")")}}]

     [:img
      {:src "http://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
       :style {:transform (str "rotate(" @s "deg) rotateY(" (+  0) "deg)" "scale(" (min 5 (max 0.1 (* 0.01 @s))) ")")}}]

     [:img
      {:src "http://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
       :style {:transform (str "rotate(" @s "deg) rotateY(" (+  @s  @spin) "deg)")}
       :on-mouse-over #(reset! s 25)
       :on-mouse-out #(reset! s 0)}]

     [:img
      {:src "http://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
       :style {:transform (str "rotate(" @s "deg) rotateY(" (+  0) "deg)")}}]

     [:img
      {:src "http://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
       :style {:transform (str "rotate(" (- @s) "deg) rotateY(" (+  0) "deg)")}}]]))



(defcard-rg widget-demo
  [demo])


(defcard-rg anim-demo
  [anim-demo])

(defcard-rg anim-demo-logo
  [anim-demo-logo])
