(ns honest-wealth-calculator.app
  (:require [honest-wealth-calculator.mortgage :as mortgage]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))

(enable-console-print!)

(def data (atom
  {:purchase-price 500000
   :total-mortgage 400000
   :interest-rate 6.5
   :number-of-years 25
   :monthly-rent-income 2500
   :ownership-cost-rate 4}))

(defn input-row [label input]
  [:div.form-group
   [:div.col-sm-6.control-label [:label label]]
   [:div.col-sm-6 input]])

(defn panel-with-heading [title body]
  [:div.panel.panel-default
    [:div.panel-heading [:h4 title]]
    [:div.panel-body body]])

(defn input [label type id]
  (input-row label [:input.form-control {:field type :id id}]))

(defn money-input [label id placeholder init-value on-change read-only]
  [:div
    [:label {:class "sr-only", :for id} label]
    [:div {:class "input-group"}
     [:div {:class "input-group-addon"} "$"]
     [:input {:type "text", :class "form-control", :step "1" :id id, :placeholder placeholder :value init-value :on-change on-change :read-only read-only}]
     [:div {:class "input-group-addon"} ".00"]]])

(defn percentage-input [label id placeholder init-value on-change read-only]
   [:div
     [:label {:class "sr-only", :for id} label]
     [:div {:class "input-group"}
      [:input {:type "text", :class "form-control", :step "1" :id id, :placeholder placeholder :value init-value :on-change on-change :read-only read-only}]
      [:div {:class "input-group-addon"} "%"]]])

(defn calc-monthly-payment []
  (let [total (@data :total-mortgage)
        interest (/ (@data :interest-rate) 100)
        months (* 12 (@data :number-of-years))
        result (mortgage/calc total interest months)]
      result))

(defn calc-ownership-cost []
  (let [total (@data :purchase-price)
        rate (/ (@data :ownership-cost-rate) 100)]
    (* total rate)))

(defn market-assumptions []
  [panel-with-heading "Market Assumptions"
    [:form "a"]])

(defn to-rent []
  (let [result (calc-monthly-payment)
        purchase-price (:purchase-price @data)
        total-mortgage (:total-mortgage @data)
        down-payment (- purchase-price total-mortgage)
        monthly (int (:monthly-payment result))
        years (:number-of-years @data)
        months (* 12 years)
        interest-cost (int (:interest-cost result))
        annual-ownership-cost (calc-ownership-cost)
        monthly-rent-income (:monthly-rent-income @data)
        annual-cost (- (+ annual-ownership-cost (/ interest-cost years)) (* monthly-rent-income 12))]
    [panel-with-heading "If rent (and invest)..."
        [:form {:class "form-horizontal"}
          (input-row "Down payment saved" [money-input "Down payment saved" :down-payment "" down-payment nil true])
          (input-row "Down payment saved" [money-input "Down payment saved" :down-payment "" down-payment nil true])
        ;   (input-row "Total mortgage" [money-input "Total mortgage" :total-mortgage "" (:total-mortgage @data) #(swap! data assoc :total-mortgage (.-target.value %)) false])
        ;   (input-row "Annual interest rate" [percentage-input "Annual interest rate" :interest-rate "" (:interest-rate @data) #(swap! data assoc :interest-rate (.-target.value %)) false])
        ;   (input-row "Number of years"
        ;     [:input.form-control {:field :numeric :id :number-of-years :value (:number-of-years @data) :on-change #(swap! data assoc :number-of-years (.-target.value %))}])
        ;   (input-row "Monthly payments" [money-input "Monthly payments" :monthly-payments "" monthly nil true])
        ;   (input-row "Total interest cost" [money-input "Interst cost" :monthly-payments "" interest-cost nil true])
        ;   (input-row "Ownership cost rate" [percentage-input "Ownership cost rate" :ownership-cost-rate "" (:ownership-cost-rate @data) #(swap! data assoc :ownership-cost-rate (.-target.value %)) false])
        ;   (input-row "Annual ownership cost" [money-input "Annual ownership cost" :ownership-cost "" annual-ownership-cost nil true])
        ;   (input-row "Monthly rent income" [money-input "Monthly rent income" :monthly-rent-income "" (:monthly-rent-income @data) #(swap! data assoc :monthly-rent-income (.-target.value %)) false])
        ;   (input-row "Annual total cost (interest + maint - rent income)" [money-input "Annual total cost" :ownership-cost "" annual-cost nil true])
          ]]))

(defn update-val-fn [var-name]
  #(swap! data assoc var-name (.-target.value %)))

(defn to-own []
  (let [result (calc-monthly-payment)
        monthly (int (:monthly-payment result))
        years (:number-of-years @data)
        months (* 12 years)
        interest-cost (int (:interest-cost result))
        annual-ownership-cost (calc-ownership-cost)
        monthly-rent-income (:monthly-rent-income @data)
        annual-pnl (- (* monthly-rent-income 12) (+ annual-ownership-cost (/ interest-cost years)))
        total-pnl (* years annual-pnl)]
    [panel-with-heading "If buy (and invest)..."
      [:form {:class "form-horizontal"}
        (input-row "Purchase price" [money-input "Purchase price" :purchase-price "" (:purchase-price @data) (update-val-fn :purchase-price) false])
        (input-row "Total mortgage" [money-input "Total mortgage" :total-mortgage "" (:total-mortgage @data) (update-val-fn :total-mortgage) false])
        (input-row "Annual interest rate" [percentage-input "Annual interest rate" :interest-rate "" (:interest-rate @data) (update-val-fn :interest-rate) false])
        (input-row "Number of years"
          [:input.form-control {:field :numeric :id :number-of-years :value (:number-of-years @data) :on-change (update-val-fn :number-of-years)}])
        (input-row "Monthly payments" [money-input "Monthly payments" :monthly-payments "" monthly nil true])
        (input-row "Total interest cost" [money-input "Interst cost" :monthly-payments "" interest-cost nil true])
        (input-row "Annual ownership cost rate (taxes, maint, rennovation)" [percentage-input "Ownership cost rate" :ownership-cost-rate "" (:ownership-cost-rate @data) (update-val-fn :ownership-cost-rate) false])
        (input-row "Annual ownership cost" [money-input "Annual ownership cost" :ownership-cost "" annual-ownership-cost nil true])
        (input-row "Monthly rent income" [money-input "Monthly rent income" :monthly-rent-income "" (:monthly-rent-income @data) (update-val-fn :monthly-rent-income) false])
        (input-row "Annual profit/loss (income - interest - maint)" [money-input "Annual total P&L" :ownership-cost "" annual-pnl nil true])
        (input-row (str "Total profit/loss (over " years " years)") [money-input "Annual total P&L" :ownership-cost "" total-pnl nil true])]]))

(defn rent-or-own-calculator []
  [:div.container
    [:h3 "To rent or to buy?"]
    [:div.row
      [:div.col-md-6 [to-own]]
      [:div.col-md-6 [to-rent]]]
    [:div.row
      [:div.col-md-12 [market-assumptions]]]])

(defn main-component []
  [:div
    [:h1 "Wealth Projector"]
    [rent-or-own-calculator]])

(defn init []
  (reagent/render-component [main-component]
                            (.getElementById js/document "container")))
