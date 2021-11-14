(ns app.views
  (:require
    [clojure.pprint :refer [pprint]]
    [clojure.string :as    s]
    [clojure.edn    :as    edn]
    [app.state :refer [!state]]
    [app.data.patterns :as p]
    [dsann.reagent.components.dynamic-css :as dcss]))

;; give a simple name to the pattern data
(def pattern-data p/patterns-3x5-8bit)

;; apply styling here
(def style
  [[:body {:font-family "Arial, Helvetica, sans-serif"
           :font-size :10px
           :color :#333}]
   [:.domino     {:fill :white
                  :stroke :black
                  :stroke-width :2px
                  :rx :10px}]
   [:.domino-dot {:fill   :#f5f5f5
                  :stroke :#f5f5f5
                  :stroke-width :2px}]
   [:.domino-dot.dot             {:fill :#CCC
                                  :stroke :darkblue}]
   [:.domino-dot.dot.symmetrical-v                             {:fill :#00CBFF   :stroke :darkyellow}]
   [:.domino-dot.dot.symmetrical-h                             {:fill :palegreen :stroke :darkgreen}]
   [:.domino-dot.dot.symmetrical-r                             {:fill :#F8D149   :stroke :#463704}]
   [:.domino-dot.dot.symmetrical-v.symmetrical-h               {:fill :purple    :stroke :darkyellow}]
   [:.domino-dot.dot.symmetrical-h.symmetrical-v.symmetrical-r {:fill :red :stroke :darkred}]

   [:.no  {:color :red}]
   [:.yes {:color :green}]

   [:div.pattern-box {:display :inline-grid
                      :justify-items :center
                      :padding :0.5em
                      :padding-bottom :2em
                      :gap :1em}]
   [:div.pattern-info {:background-color :white
                       :padding :0.2em
                       :display :grid
                       :gap :0.5em
                       :grid-template-columns "auto auto"}]
   [:div.pattern-value {:grid-column "1 / -1"
                        :text-align :center}]
   [:div.hvr-filter {:padding :0.5em
                     :display :inline-grid
                     :grid-template-columns "auto auto auto auto auto auto"

                     :border  "1px solid darkgray"
                     :gap :1em}
      [:.input {:padding-right :3em}]]

   ["label[for=unsymmetrical]" {:color :red}]
   [:.warning {:color :red
               :display :inline-block
               :padding :0.5em
               ::margin-bottom :0.5em
               :border "1px solid darkred"}]])

;; makes some unicode characters available
;; https://unicode-table.com/en/sets/check/
(def unicode
  {:yes (edn/read-string "\\u2713")
   :no  (edn/read-string "\\u2717")
   :right-arrow (edn/read-string "\\u21E8")})


;; pretty print to a pre tag
(defn pre-pp [v] [:pre (with-out-str (pprint v))])


;; create svg for the domino
(defn pattern-svg [line-size pattern-data & {:keys [svg-pad]
                                             :or   {svg-pad 0}}]

  (let [w (* 4 line-size)
        h (* 6 line-size)
        dot-radius (* line-size 0.5 0.75)
        {:keys [pattern symmetrical? symmetrical-h? symmetrical-v? symmetrical-r?]} pattern-data
        svg-width  (+ w (* 2 svg-pad))
        svg-height (+ h (* 2 svg-pad))]
   [:div.pattern-svg
     [:svg {:width svg-width :height svg-height}
       ;; move it away from the svg borders
       [:g {:transform (str "translate(" svg-pad " " svg-pad  ")")}

        ;; the rectange
         [:rect.domino
          {:x 0 :y 0 :width w :height h}]

         ;; the dots
         (for [[r row ]      (map-indexed vector pattern)
               [c dot-value] (map-indexed vector row)]
           (let [dot? (= 1 dot-value)]
             ^{:key [r c]}
             [:circle.domino-dot
              ;; use these classes for styling
              {:class [(when symmetrical?   :symmetrical)
                       (when symmetrical-h? :symmetrical-v)
                       (when symmetrical-v? :symmetrical-h)
                       (when symmetrical-r? :symmetrical-r)
                       (when dot?         :dot)]
               :cy (* line-size (inc r)) :cx (* line-size (inc c)) :r dot-radius}]))]]]))

;; a y/n check mark
(defn yes-no [yes?]
  (if yes?
     [:span {:class :yes} (:yes unicode)]
     [:span {:class :no}  (:no unicode)]))

(def symmetry-names
  {:symmetrical-h? "horizontal"
   :symmetrical-v? "vertical"
   :symmetrical-r? "rotational"})

;; display the pattern as a domino with some additional data
(defn pattern-box [line-size pattern-data]
  [:div.pattern-box
    [pattern-svg line-size pattern-data :svg-pad 5]
    [:div.pattern-info
      [:div.pattern-value (:i pattern-data)]
      [:div.pattern-value (s/join (:binary-vec pattern-data))]
      (for [k [:symmetrical-h? :symmetrical-v? :symmetrical-r?]]
        ^{:key k}
        [:<>
          [:div.pattern-field (symmetry-names k)]
          [:div.pattern-field-check [yes-no (k pattern-data)]]])
      [:div.pattern-field "flip h"]
      [:div.pattern-field (:pattern-h-i pattern-data)]
      [:div.pattern-field "flip v"]
      [:div.pattern-field (:pattern-v-i pattern-data)]
      [:div.pattern-field "rotate"]
      [:div.pattern-field (:pattern-r-i pattern-data)]]])


;; filtering the various symmetries

(defn change-hvr-filter [filter-id event]
  (swap! !state assoc :hvr-filter filter-id))

(defn hvr-radio [id id-f state]
  [:<>
     [:label {:for id} (name id)]
     [:div.input
       [:input {:type :radio :name :hvr-filter :id id :default-checked (= id-f state)
                :on-change (partial change-hvr-filter id-f)}]]])

(defn hvr-filter-box [state]
  [:<>
   [:div
    [:div.warning
     "warning: not optimised for speed, it can take a minute if you click "
     [:strong [:em "unsymmetrical"]]
     "..."]]

   ;; create a bunch of radio buttons
   [:div.hvr-filter
     (for [[id id-f] (partition 2 [:symmetrical     :symmetrical?
                                   :symmetrical-hvr :symmetrical-hvr
                                   :unsymmetrical   :unsymmetrical?
                                   :symmetrical-h   :symmetrical-h?
                                   :symmetrical-v   :symmetrical-v?
                                   :symmetrical-r   :symmetrical-r?
                                   :symmetrical-h!!  :symmetrical-h!!
                                   :symmetrical-!v!  :symmetrical-!v!
                                   :symmetrical-!!r :symmetrical-!!r])]
       ^{:key id}
       [hvr-radio id id-f state])]])

;; the main page
(defn main []
  (let [state @!state
        {:keys [hvr-filter]} state
        patterns         (:patterns        pattern-data)
        patterns-by-i    (:patterns-by-i   pattern-data)
        pattern-numbers  (:pattern-numbers pattern-data)
        counts           (:pattern-counts  pattern-data)]
    [:<>
     ;; allo css to update
     [dcss/dynamic-css :dynamic-style style]

     ;; the counts
     [:div
      "Counts of patterns and their symmetries"
      [pre-pp (into (sorted-map) counts)]]
     ;; the filter box
     [hvr-filter-box hvr-filter]

     ;; all the patterns
     [:div.all-patterns
       (for [i pattern-numbers]
         (let [p (patterns-by-i i)]
           ; include p only if it matches the filter
           (when (get p hvr-filter)
             ^{:key i}
             [pattern-box 20 p])))]]))
