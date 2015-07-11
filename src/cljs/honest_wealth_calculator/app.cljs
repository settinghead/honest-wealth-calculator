(ns honest-wealth-calculator.app
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))


(defn to-rent []
  [:div
  [:h4 "If I rent..."]])

(defn to-own []
  [:div
  [:h4 "If I own..."]])

(defn rent-or-own-calculator []
  [:div.container
   [:h3 "To Rent or to Own?"]
   [:div.row
    [:div.col-md-6 [to-rent]]
    [:div.col-md-6 [to-own]]]])

(defn main-component []
  [:div
    [:h1 "The Honest Wealth Calculator"]
    [rent-or-own-calculator]])

(defn init []
  (reagent/render-component [main-component]
                            (.getElementById js/document "container")))
