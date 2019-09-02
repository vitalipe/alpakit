(ns cards.animation.spring
  (:require
    [reagent.core :as r]
    [devcards.core]
    [devcards.core :refer-macros [defcard-rg]]

    [alpakit.core   :refer [surface element]]
    [alpakit.layout :as layout :refer [v-box]]
    [alpakit.animation :refer [spring+control] :as animation]
    [alpakit.animation.macros    :refer-macros [+>]]))



(defcard-rg image-zoom
  "http://facebook.github.io/rebound-js/examples/"
  (let [[s cfg] (spring+control :to 1 :damping 2)]
    (fn []
        [surface :layout (layout/h-box :justify :space-around)
                 :-attr {:on-mouse-down #(swap! cfg assoc :to 0.5)
                         :on-mouse-up   #(swap! cfg assoc :to 1)
                         :on-mouse-out   #(swap! cfg assoc :to 1)}

         [element
          :e :img
          :-attr {:src "http://facebook.github.io/rebound-js/examples/photoScale/landscape.jpg"
                  :draggable false}
          :css {:transform {:scale-x s
                            :scale-y s}
                :user-select "none"}]])))



(defcard-rg springy-menu
  (let [[s cfg] (spring+control :from 0 :to 0 :stiffness 0.8 :damping 1 :mass 10)
        s*100px  (+> s (map (partial *  100))  (map #(str % "px")))
        s*-100px (+> s (map (partial *  -100)) (map #(str % "px")))
        s*30deg  (+> s (map (partial *   30)) (map #(str % "deg")))
        s*90-90deg  (+> s
                      (map (partial * 90))
                      (map (partial - 90))
                      (map #(str % "deg")))]
    (fn []
      [surface :layout (layout/h-box :justify :space-around)
               :style {:height "200px"}

       [surface :css {:width "60px"
                      :height "60px"
                      :position "relative"
                      :cursor   "pointer"
                      :user-select "none"}

                :-attr {:on-click #(if (zero? (:to @cfg))
                                     (swap! cfg assoc :to 1 :damping 1)
                                     (swap! cfg assoc :to 0 :damping 4))}


        [surface
         :layout (layout/v-box :justify :space-around)
         :css {:background-color "lightblue"
               :width  "100%"
               :height "100%"
               :position "absolute"
               :top 0
               :left 0
               :z-index 1
               :text-align "center"}

         :transform {:translate-x s*-100px
                     :translate-y s*100px
                     :rotate-z    s*90-90deg}
            "C"]

        [surface :layout (layout/v-box :justify :space-around)
                 :css {:background-color "lightblue"
                       :width  "100%"
                       :height "100%"
                       :position "absolute"
                       :top 0
                       :left 0
                       :z-index 1
                       :text-align "center"}
                 :transform  {:translate-y s*100px
                              :rotate-z    s*90-90deg}
              "B"]

        [surface :layout (layout/v-box :justify :space-around)
                 :css {:background-color "lightblue"
                       :width  "100%"
                       :height "100%"
                       :position "absolute"
                       :top 0
                       :left 0
                       :z-index 1
                       :text-align "center"}
                 :transform  {:translate-x  s*100px
                              :translate-y s*100px
                              :rotate-z    s*90-90deg}

              "A"]

        [surface :layout (layout/v-box :justify :space-around)
                 :css {:background-color "lightgreen"
                       :width "100%"
                       :height "100%"
                       :position "relative"
                       :top 0
                       :left 0
                       :text-align "center"
                       :z-index 2}
                 :transform {:rotate-z s*30deg}

            [element :style {:opacity (- 1 (:to @cfg))
                             :transition-property "opacity"
                             :transition-duration "100ms"
                             :position "relative"}
             "OPEN"]

            [element :style {:opacity (:to @cfg)
                             :transition-property "opacity"
                             :transition-duration "100ms"
                             :position "absolute"}
             "CLOSE"]]]])))


(defcard-rg springy-double-box
  (let [[s cfg]   (spring+control :from 0 :to 0 :stiffness 2 :damping 1.5 :mass 100)]
    (fn []
      [surface :layout (layout/h-box :justify :space-around)
       [surface :css {:width "290px";
                      :height "250px";
                      :position "relative"}

                :-attr {:on-mouse-enter #(reset! s 10)
                        :on-mouse-leave #(reset! s 0)}

        [surface :css {:background-color "lightblue"
                       :width  "100%"
                       :height "100%"
                       :position "absolute"
                       :top 0
                       :left 0
                       :z-index 1}
                 :transform {:translate-x (str (* 10 @s) "px")
                             :translate-y (str (* -10 @s) "px")
                             :rotate-z    (str @s "deg")}]

        [surface :css {:background-color "lightgreen"
                       :width "100%"
                       :height "100%"
                       :position "relative"
                       :top 0
                       :left 0
                       :z-index 2}
                 :transform  {:rotate-z (str (* -1 @s) "deg")}]]])))




(defcard-rg springy-flipcard
  (let [[s cfg]   (spring+control :from 0 :to 0 :stiffness 2 :damping 20 :mass 100)]
    (fn []
      [surface :layout (layout/h-box :justify :space-around)
       [surface :css {:width "300px";
                      :height "300px";
                      :perspective "1000px"}

                :-attr {:on-mouse-enter #(reset! s 180)
                        :on-mouse-leave #(reset! s 0)}

        [surface :css {:position "relative"
                       :transform-style "preserve-3d"
                       :width "300px"
                       :height "300px"}
                 :transform {:rotate-y (str @s "deg")}

          [element :css {:background-color "lightgreen"
                         :width "100%"
                         :height "100%"
                         :backface-visibility "hidden"
                         :position "absolute"
                         :top 0
                         :left 0
                         :z-index 2}]

          [element :css {:background-color "lightblue"
                         :width "100%"
                         :height "100%"
                         :backface-visibility "hidden"
                         :position "absolute"
                         :top 0
                         :left 0
                         :transform {:rotate-y "180deg"}}]]]])))


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
