(ns ^:figwheel-no-load app.state
  (:require 
    [reagent.core :as r]))

(def !state (r/atom {:hvr-filter :symmetrical?}))


