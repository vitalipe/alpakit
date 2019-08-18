(ns cards.animation
  (:require
    [reagent.core :as r]
    [devcards.core]
    [devcards.core :refer-macros [defcard-rg]]

    [alpakit.core   :refer [surface element]]
    [alpakit.layout :as layout :refer [v-box]]
    [alpakit.animation :refer [spring+control] :as animation]))


(defcard-rg springy-menu
  (let [[s cfg]   (spring+control :from 0 :to 0 :stiffness 0.8 :damping 1 :mass 10)]
    (fn []
      [surface :layout (layout/h-box :justify :space-around)
               :style {:height "200px"}
       [:div.card {:style {:width "50px";
                           :height "50px";
                           :position "relative"
                           :cursor   "pointer"}

                   :on-click #(if (zero? (:to @cfg))
                                (swap! cfg assoc :to 1 :damping 1)
                                (swap! cfg assoc :to 0 :damping 4))}


        [:div.back {:style {:background-color "lightblue"
                            :width  "100%"
                            :height "100%"
                            :position "absolute"
                            :top 0
                            :left 0
                            :z-index 1
                            :transform (str "translate(" (* -100 @s) "px, " (* 100 @s) "px)"
                                              "rotateZ(" (* 30 @s) "deg)")}}]

        [:div.back {:style {:background-color "lightblue"
                                :width  "100%"
                                :height "100%"
                                :position "absolute"
                                :top 0
                                :left 0
                                :z-index 1
                                :transform (str "translate(0px, " (* 100 @s) "px)"
                                                "rotateZ(" (* 50 @s) "deg)")}}]

        [:div.back {:style {:background-color "lightblue"
                                :width  "100%"
                                :height "100%"
                                :position "absolute"
                                :top 0
                                :left 0
                                :z-index 1
                            :transform (str "translate(" (* 100 @s) "px, " (* 100 @s) "px)"
                                            "rotateZ(" (* 70 @s) "deg)")}}]

        [:div.front {:style {:background-color "lightgreen"
                             :width "100%"
                             :height "100%"
                             :position "relative"
                             :top 0
                             :left 0
                             :z-index 2
                             :transform (str "rotateZ(" (* -20 @s) "deg)")}}]]])))


(defcard-rg springy-double-box
  (let [[s cfg]   (spring+control :from 0 :to 0 :stiffness 2 :damping 1.5 :mass 100)]
    (fn []
      [surface :layout (layout/h-box :justify :space-around)
       [:div.card {:style {:width "290px";
                           :height "250px";
                           :position "relative"}

                   :on-mouse-enter #(reset! s 10)
                   :on-mouse-leave #(reset! s 0)}

        [:div.back {:style {:background-color "lightblue"
                            :width  "100%"
                            :height "100%"
                            :position "absolute"
                            :top 0
                            :left 0
                            :z-index 1
                            :transform (str "translate(" (* 10 @s) "px, " (* -10 @s) "px)" "rotateZ(" (.floor js/Math @s) "deg)")}}]

        [:div.front {:style {:background-color "lightgreen"
                             :width "100%"
                             :height "100%"
                             :position "relative"
                             :top 0
                             :left 0
                             :z-index 2
                             :transform (str "rotateZ(" (- (.floor js/Math @s)) "deg)")}}]]])))




(defcard-rg springy-flipcard
  (let [[s cfg]   (spring+control :from 0 :to 0 :stiffness 2 :damping 10 :mass 100)]
    (fn []
      [surface :layout (layout/h-box :justify :space-around)
       [:div.card {:style {:width "300px";
                           :height "300px";
                           :perspective "1000px"}

                   :on-mouse-enter #(reset! s 180)
                   :on-mouse-leave #(reset! s 0)}

        [:div.flipper {:style {:position "relative"
                               :transform-style "preserve-3d"
                               :width "300px"
                               :height "300px"
                               :transform (str "rotateY(" (.floor js/Math @s) "deg)")}}

          [:div.front {:style {:background-color "lightgreen"
                               :width "100%"
                               :height "100%"
                               :backface-visibility "hidden"
                               :position "absolute"
                               :top 0
                               :left 0
                               :z-index 2}}]

          [:div.back {:style {:background-color "lightblue"
                              :width "100%"
                              :height "100%"
                              :backface-visibility "hidden"
                              :position "absolute"
                              :top 0
                              :left 0
                              :transform "rotateY(180deg)"}}]]]])))


(defcard-rg springy-image
  (let [with-interval! (fn [timeMs anim f]
                         (js/setInterval #(swap! anim f) timeMs)
                         anim)

        [spin cfg]  (spring+control)
        y-axis      (with-interval! 1000 (animation/spring :to 30 :damping 0.7) #(if (= 30 %) 0 30))]
    (fn []
      [surface :layout (v-box)

       [:div "spin: " @spin]
       [:div "y-axis: " @y-axis]

       [element :style {:background-color "lightblue"
                        :width (str (+ 100 @spin) "px")}
          "s"]
       [element :style {:background-color "gold"
                        :width (str (+ 100 @y-axis) "px")}
          "y-axis"]

       [surface :layout (v-box :align :center)
        [:img
         {:src "http://timothypratley.github.io/reanimated/img/monster_zombie_hand-512.png"
          :style {:transform (str "rotate(" @spin "deg) rotateY(" (+  @y-axis) "deg)")}
          :on-mouse-over #(reset! spin 25)
          :on-mouse-out #(reset! spin 0)}]]])))
