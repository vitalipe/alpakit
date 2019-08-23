(ns cards.core
  (:require
    [reagent.core :as r]
    [devcards.core]

    [alpakit.css :refer [style-sheet]]

    ;; cards
    [cards.animation.spring]
    [cards.layout.grid]))

(enable-console-print!)

;; setup css
(let [root (.createElement js/document "div")]
  (.appendChild (.-body js/document) root)
  (r/render [style-sheet] root))
