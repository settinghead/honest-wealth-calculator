(ns honest-wealth-calculator.mortgage)

(defn calc [amount interest months]
  (let [monthly-interest (/ interest 12)
        acc (* monthly-interest (.pow js/Math (+ 1 monthly-interest) months))
        compound (- (.pow js/Math (+ 1 monthly-interest) months) 1)]
    (* amount (/ acc compound))))
