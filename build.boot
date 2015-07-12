(set-env!
 :source-paths    #{"sass" "src/cljs" "src/clj"}
 :resource-paths  #{"resources"}
 :dependencies '[[adzerk/boot-cljs      "0.0-2814-4" :scope "test"]
                 [adzerk/boot-cljs-repl "0.1.9"      :scope "test"]
                 [adzerk/boot-reload    "0.2.4"      :scope "test"]
                 [pandeiro/boot-http    "0.6.1"      :scope "test"]
                 [reagent "0.5.0"]
                 [reagent-forms "0.5.1"]
                 [boot-garden "1.2.5-1" :scope "test"]
                 [mathias/boot-sassc  "0.1.1" :scope "test"]])

(require
 '[adzerk.boot-cljs      :refer [cljs]]
 '[adzerk.boot-cljs-repl :refer [cljs-repl start-repl]]
 '[adzerk.boot-reload    :refer [reload]]
 '[pandeiro.boot-http    :refer [serve]]
 '[boot-garden.core    :refer [garden]]
 '[mathias.boot-sassc  :refer [sass]])

(deftask build []
  (comp (speak)

        (cljs)

        (garden :styles-var 'honest-wealth-calculator.styles/screen
:output-to "css/garden.css")
        (sass :output-dir "css")))

(deftask run []
  (comp (serve)
        (watch)
        (cljs-repl)
        (reload)
        (build)))

(deftask production []
  (task-options! cljs {:optimizations :advanced
                       ;; pseudo-names true is currently required
                       ;; https://github.com/martinklepsch/pseudo-names-error
                       ;; hopefully fixed soon
                       :pseudo-names true}
                      garden {:pretty-print false}
                      sass   {:output-style "compressed"})
  identity)

(deftask development []
  (task-options! cljs {:optimizations :none
                       :unified-mode true
                       :source-map true}
                 reload {:on-jsload 'honest-wealth-calculator.app/init}
                      sass   {:line-numbers true
                                     :source-maps  true})
  identity)

(deftask dev
  "Simple alias to run application in development mode"
  []
  (comp (development)
        (run)))

(deftask prod
  "Simple alias to run application in development mode"
  []
  (comp (production)
        (build)))
