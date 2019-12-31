(ns cards.core
  (:require
    [reagent.core :as r]
    [devcards.core]

    [alpakit.core :refer [app render!]]

    ;; cards
    [cards.animation.spring]
    [cards.layout.grid]))

(enable-console-print!)

;; setup
(render! [app :theme {}])
