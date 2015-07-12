(ns honest-wealth-calculator.app
  (:require [honest-wealth-calculator.mortgage :as mortgage]
            [reagent.core :as reagent :refer [atom]]
            [reagent-forms.core :refer [bind-fields init-field value-of]]))

(enable-console-print!)

(def data (atom
  {:purchase-price 500000
   :total-mortgage 400000
   :interest-rate 5.0
   :number-of-years 25
   :monthly-lease-income 2500
   :monthly-rent 2500
   :percentage-invest 100
   :ownership-cost-rate 3}))

(defn input-row [label input]
  [:div.form-group
   [:div.col-sm-7.control-label [:label label]]
   [:div.col-sm-5 input]])

(defn panel-with-heading [title body]
  [:div.panel.panel-default
    [:div.panel-heading [:h4 title]]
    [:div.panel-body body]])

(defn update-val-fn [var-name]
  #(swap! data assoc var-name (.-target.value %)))

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

(defn slider [param value min max]
  [:input {:type "range" :value value :min min :max max
           :style {:width "100%"}
           :on-change (fn [e]
                        (swap! data assoc param (.-target.value e))
                        (when (not= param :bmi)
                          (swap! data assoc :bmi nil)))}])

(defn to-rent []
  (let [result (calc-monthly-payment)
        purchase-price (:purchase-price @data)
        total-mortgage (:total-mortgage @data)
        total-down-payment (- purchase-price total-mortgage)
        percentage-invest (:percentage-invest @data)
        money-to-invest (/ (* total-down-payment percentage-invest) 100)
        monthly (int (:monthly-payment result))
        years (:number-of-years @data)
        months (* 12 years)
        interest-cost (int (:interest-cost result))
        annual-ownership-cost (calc-ownership-cost)
        monthly-rent (:monthly-rent @data)]
    [panel-with-heading "If rent (and invest)..."
        [:form {:class "form-horizontal"}
          (input-row "Down payment saved" [money-input "Down payment saved" :down-payment "" total-down-payment nil true])
        ;   (input-row "Total mortgage" [money-input "Total mortgage" :total-mortgage "" (:total-mortgage @data) #(swap! data assoc :total-mortgage (.-target.value %)) false])
        ;   (input-row "Annual interest rate" [percentage-input "Annual interest rate" :interest-rate "" (:interest-rate @data) #(swap! data assoc :interest-rate (.-target.value %)) false])
          (input-row "Number of years"
            [:input.form-control {:field :numeric :id :number-of-years :value (:number-of-years @data) :read-only true}])
          (input-row "Percentage of down payment to be used in alternative investments"
            [:div
              [slider :percentage-invest percentage-invest 0 100]
              (str percentage-invest "%")])
          (input-row "Money to invest" [money-input "Money to invest" :money-to-invest "" money-to-invest nil true])
          (input-row "Monthly rent" [money-input "Monthly rent" :monthly-rent "" monthly-rent (update-val-fn :monthly-rent) false])
        ;   (input-row "Monthly payments" [money-input "Monthly payments" :monthly-payments "" monthly nil true])
        ;   (input-row "Total interest cost" [money-input "Interst cost" :monthly-payments "" interest-cost nil true])
        ;   (input-row "Ownership cost rate" [percentage-input "Ownership cost rate" :ownership-cost-rate "" (:ownership-cost-rate @data) #(swap! data assoc :ownership-cost-rate (.-target.value %)) false])
        ;   (input-row "Annual ownership cost" [money-input "Annual ownership cost" :ownership-cost "" annual-ownership-cost nil true])
        ;   (input-row "Monthly rent income" [money-input "Monthly rent income" :monthly-lease-income "" (:monthly-lease-income @data) #(swap! data assoc :monthly-lease-income (.-target.value %)) false])
        ;   (input-row "Annual total cost (interest + maint - rent income)" [money-input "Annual total cost" :ownership-cost "" annual-cost nil true])
          ]]))

(defn to-own []
  (let [purchase-price (:purchase-price @data)
        result (calc-monthly-payment)
        monthly (int (:monthly-payment result))
        years (:number-of-years @data)
        months (* 12 years)
        interest-cost (int (:interest-cost result))
        annual-ownership-cost (calc-ownership-cost)
        monthly-ownership-cost (/ annual-ownership-cost 12)
        monthly-lease-income (:monthly-lease-income @data)
        monthly-total-cost (+ monthly-ownership-cost (/ interest-cost months))
        monthly-pnl (- monthly-lease-income monthly-total-cost)
        annual-pnl (- (* monthly-lease-income 12) (+ annual-ownership-cost (/ interest-cost years)))
        total-pnl (* years annual-pnl)
        total-appreciation-gain ()]
    [panel-with-heading "If buy (and invest)..."
      [:form {:class "form-horizontal"}
        (input-row "Purchase price" [money-input "Purchase price" :purchase-price "" purchase-price (update-val-fn :purchase-price) false])
        (input-row "Total mortgage" [money-input "Total mortgage" :total-mortgage "" (:total-mortgage @data) (update-val-fn :total-mortgage) false])
        (input-row "Average annual interest rate" [percentage-input "Annual interest rate" :interest-rate "" (:interest-rate @data) (update-val-fn :interest-rate) false])
        (input-row "Number of years"
          [:input.form-control {:field :numeric :id :number-of-years :value (:number-of-years @data) :on-change (update-val-fn :number-of-years)}])
        (input-row "Monthly mortgage you pay" [money-input "Monthly mortgage payment" :monthly-payments "" monthly nil true])
        (input-row (str "Total interest you pay (over " years " years)") [money-input "Interst cost" :monthly-payments "" interest-cost nil true])
        (input-row "Annual ownership cost rate (taxes + maint. fees + rennovation + etc, as a percentage of total purchase price)" [percentage-input "Ownership cost rate" :ownership-cost-rate "" (:ownership-cost-rate @data) (update-val-fn :ownership-cost-rate) false])
        (input-row "Annual ownership cost" [money-input "Annual/monthly ownership cost" :ownership-cost "" annual-ownership-cost nil true])
        (input-row "Monthly ownership cost" [money-input "Monthly ownership cost" :monthly-ownership-cost "" monthly-ownership-cost nil true])
        (input-row "total monthly cost" [money-input "Monthly ownership cost" :monthly-total-cost "" monthly-total-cost nil true])
        (input-row "Monthly income you get from leasing" [money-input "Monthly rent income" :monthly-lease-income "" (:monthly-lease-income @data) (update-val-fn :monthly-lease-income) false])
        (input-row "Total monthly profit/loss (income - cost)" [money-input "Monthly P&L" :monthly-pnl "" monthly-pnl nil true])
        (input-row "Total annual profit/loss" [money-input "Annual total P&L" :ownership-cost "" annual-pnl nil true])
        (input-row (str "Total profit/loss (over " years " years)") [money-input "Annual total P&L" :annual-pnl "" total-pnl nil true])
        (input-row "Total gain from appreciation" [money-input "" :total-appreciation-gain "" "TODO" nil true])]]))

(defn rent-or-own-calculator []
  [:div.container
    [:div.row
      [:div.col-md-6 [to-own]]
      [:div.col-md-6 [to-rent]]]
    [:div.row
      [:div.col-md-12 [market-assumptions]]]])

(defn main-component []
  [:div
    [:h2 "Rent vs. Buy Calculator"]
    [rent-or-own-calculator]])

(defn init []
  (reagent/render-component [main-component]
                            (.getElementById js/document "container")))
