(ns honest-wealth-calculator.app
  (:require [honest-wealth-calculator.mortgage :as mortgage]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))

(enable-console-print!)

(def data (atom
  {:total-mortgage 400000
   :interest-rate 0.048
   :number-of-years 25}))

(defn input-row [label input]
  [:div.row
   [:div.col-md-2 [:label label]]
   [:div.col-md-5 input]])

(defn input [label type id]
  (input-row label [:input.form-control {:field type :id id}]))

(defn money-input [label id placeholder init-value on-change]
    [:div {:class "form-group"}
        [:label {:class "sr-only", :for id} label]
        [:div {:class "input-group"}
         [:div {:class "input-group-addon"} "$"]
         [:input {:type "text", :class "form-control", :id id, :placeholder placeholder :value init-value :on-change on-change}]
         [:div {:class "input-group-addon"} ".00"]]])

(defn to-rent []
  [:div
  [:h4 "If I rent..."]])

(defn calc-monthly-payment []
  (let [total (@data :total-mortgage)
        interest (@data :interest-rate)
        months (* 12 (@data :number-of-years))]
      (mortgage/calc total interest months)))

(defn to-own []
  [:form {:class "form"}
    [:h4 "If I own..."]
    (input-row "Total mortgage" [money-input "Total mortgage" :total-mortgage "" (:total-mortgage @data) #(swap! data assoc :total-mortgage (.-target.value %))])
    (input-row "Interest rate"
      [:input.form-control {:field :numeric :id :interest-rate :value (:interest-rate @data) :on-change #(swap! data assoc :interest-rate (.-target.value %))}])
    (input-row "Number of years"
      [:input.form-control {:field :numeric :id :number-of-years :value (:number-of-years @data)}])
    (input-row "Monthly payments" [:input.form-control {:field :numeric :fmt "%.2f" :id :bmi :read-only true :value (calc-monthly-payment)}])])

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
