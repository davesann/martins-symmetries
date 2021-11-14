(ns figwheel.build
  (:require [figwheel.main.api :as fig]))

(def builds
  {:dev {:id "dev"
         :options {:main 'app.main
                   :source-map true}
         :config  {:watch-dirs ["src/main/clj"]}}
   :prod {:id "prod"
          :options {:output-to     "releases/v1/js/main.js"
                    :optimizations :advanced
                    :infer-externs true
                    :source-map    "releases/v1/js/main.js.map"
                    :output-dir    "releases/v1/js/target"
                    :main 'app.main}
          :config {:watch-dirs ["src/main/clj"]}}})


(defn start [id & ids]
  (when-let [build (builds id)]
    (let [fig-conf
            {:mode :serve
             :log-file "figwheel-main.log"}]

      (apply fig/start fig-conf build (map builds ids))
      (fig/cljs-repl (:id build)))))


(defn stop [& ids]
  (doseq [id ids]
    (fig/stop (-> builds id :id))))

(defn stop-all []
  (fig/stop-all))

(comment

  ;; stop only works when sent to clj repl not the cljs repl.
  ;; evaluate :cljs/quit to exit the cljs repl

  (start :dev)

  ;; start dev and background prod
  (start :dev :prod)

  :cljs/quit

  ;; stop everything
  (fig/stop-all)

  (stop-all)

  nil)
