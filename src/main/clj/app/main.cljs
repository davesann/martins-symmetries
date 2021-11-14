(ns app.main
  (:require
    [reagent.dom :as rd]
    [app.views :refer [main]]))

(def app (js/document.getElementById "app"))

(rd/render [main] app)







