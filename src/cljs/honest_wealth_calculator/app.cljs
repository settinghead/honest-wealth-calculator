(ns honest-wealth-calculator.app
  (:require [honest-wealth-calculator.mortgage :as mortgage]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))

(enable-console-print!)

(def data (atom
  {:purchase-price 500000
   :total-mortgage 400000
   :interest-rate 0.065
   :number-of-years 25
   :ownership-cost-rate 0.04}))

(defn input-row [label input]
  [:div.form-group
   [:div.col-sm-2.control-label [:label label]]
   [:div.col-sm-10 input]])

(defn input [label type id]
  (input-row label [:input.form-control {:field type :id id}]))

(defn money-input [label id placeholder init-value on-change read-only]
  [:div
    [:label {:class "sr-only", :for id} label]
    [:div {:class "input-group"}
     [:div {:class "input-group-addon"} "$"]
     [:input {:type "text", :class "form-control", :step "1" :id id, :placeholder placeholder :value init-value :on-change on-change :read-only read-only}]
     [:div {:class "input-group-addon"} ".00"]]])

(defn to-rent []
  [:div
  [:h4 "If I rent..."]])

(defn calc-monthly-payment []
  (let [total (@data :total-mortgage)
        interest (@data :interest-rate)
        months (* 12 (@data :number-of-years))
        result (mortgage/calc total interest months)]
      result))

(defn calc-ownership-cost []
  (let [total (@data :purchase-price)
        rate (@data :ownership-cost-rate)]
    (* total rate)))

(defn to-own []
  (let [result (calc-monthly-payment)
        monthly (int (:monthly-payment result))
        interest-cost (int (:interest-cost result))
        ownership-cost (calc-ownership-cost)]
    [:form {:class "form-horizontal"}
      [:h4 "If I own..."]
      (input-row "Purchase price" [money-input "Purchase price" :purchase-price "" (:purchase-price @data) #(swap! data assoc :purchase-price (.-target.value %)) false])
      (input-row "Total mortgage" [money-input "Total mortgage" :total-mortgage "" (:total-mortgage @data) #(swap! data assoc :total-mortgage (.-target.value %)) false])
      (input-row "Interest rate"
        [:input.form-control {:field :numeric :id :interest-rate :value (:interest-rate @data) :on-change #(swap! data assoc :interest-rate (.-target.value %))}])
      (input-row "Number of years"
        [:input.form-control {:field :numeric :id :number-of-years :value (:number-of-years @data) :on-change #(swap! data assoc :number-of-years (.-target.value %))}])
      (input-row "Monthly payments" [money-input "Monthly payments" :monthly-payments "" monthly nil true])
      (input-row "Total interest cost" [money-input "Interst cost" :monthly-payments "" interest-cost nil true])
      (input-row "Ownership cost rate"
        [:input.form-control {:field :numeric :id :ownership-cost-rate :value (:ownership-cost-rate @data) :on-change #(swap! data assoc :ownership-cost-rate (.-target.value %))}])
      (input-row "Annual ownership cost" [money-input "Annual ownership cost" :ownership-cost "" (calc-ownership-cost) nil true])
      ]))

(defn rent-or-own-calculator []
  [:div.container
   [:h3 "To Rent or to Own?"]
   [:div.row
    [:div.col-md-6 [to-rent]]
    [:div.col-md-6 [to-own]]]])

(defn main-component []
  [:div
    [:h1 "Wealth Projector"]
    [rent-or-own-calculator]])

(defn init []
  (reagent/render-component [main-component]
                            (.getElementById js/document "container")))
