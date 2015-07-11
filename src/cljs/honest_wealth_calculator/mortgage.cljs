(ns honest-wealth-calculator.mortgage)

(defn calc [amount interest months]
  (let [monthly-interest (/ interest 12)
        acc (* monthly-interest (.pow js/Math (+ 1 monthly-interest) months))
        compound (- (.pow js/Math (+ 1 monthly-interest) months) 1)
        monthly-payment (* amount (/ acc compound))]
    {:monthly-payment monthly-payment
     :interest-cost (- (* monthly-payment months) amount)}))
