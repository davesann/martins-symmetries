(ns app.data.patterns
  (:require 
    [dsann.let-map :refer [let-map]]
    [app.data.fns :as f]))


(def patterns-3x5-8bit
  (let-map
    lowest  2r11111111
    highest (f/exp 2 15)

    ;[16384 8192 4096 2048 1024 512 256 128 64 32 16 8 4 2 1]
    powers-of-2   (reverse (take 15 (iterate (fn [x] (* 2 x)) 1)))
    binary-vec-15 (fn [i] (f/binary-vec powers-of-2 i))

    _patterns (into []
                (comp
                  ;;find the bitcount
                  (map    (fn [i]     [i (f/bitcount i)]))
                  ;; keep those with 8 1s
                  (filter (fn [[i b]] (= 8 b)))
                  ;; generate the patern data
                  (map    (fn [[i b]] (let-map
                                         ; the number
                                         i               i
                                         
                                         ; the bitcount
                                         bitcount        b
                                        
                                         ; number as a binary string
                                         binary-str      (f/binary-str i)
                                         
                                         ; number as a vector of 0 and 1 (length 15) 
                                         binary-vec      (binary-vec-15 i)

                                         ; partition as groups of 3 (there will be 5)
                                         pattern         (partition 3 binary-vec)

                                         ; flip horizontally
                                         pattern-h       (f/flip-horizontal pattern)

                                         ;flip vertically 
                                         pattern-v       (f/flip-vertical   pattern)

                                         ; rotate
                                         pattern-r       (f/flip-vertical   pattern-h) ;; h + v -> r

                                         ;; is it symmetrical?
                                         symmetrical-h?  (= pattern pattern-h)
                                         symmetrical-v?  (= pattern pattern-v)
                                         symmetrical-r?  (= pattern pattern-r)

                                         ; different symmetry combinations
                                         symmetrical?    (or symmetrical-h?  symmetrical-v?  symmetrical-r?)
                                         unsymmetrical?  (not symmetrical?)
                                         symmetrical-h!! (and      symmetrical-h?  (not symmetrical-v?) (not symmetrical-r?))
                                         symmetrical-!v! (and (not symmetrical-h?)      symmetrical-v?  (not symmetrical-r?))
                                         symmetrical-!!r (and (not symmetrical-h?) (not symmetrical-v?)      symmetrical-r?)
                                         symmetrical-hv! (and      symmetrical-h?       symmetrical-v?  (not symmetrical-r?))
                                         symmetrical-h!r (and      symmetrical-h?  (not symmetrical-v?)      symmetrical-r?)
                                         symmetrical-!vr (and (not symmetrical-h?)      symmetrical-v?       symmetrical-r?)
                                         symmetrical-hvr (and      symmetrical-h?       symmetrical-v?       symmetrical-r?)))))
                ;; do each of the above for every number in the range lowest to highest -1
                (range lowest highest))

    ;; cross link the patterns and index by number
    patterns-by-i   (-> _patterns f/index-by-pattern f/link-indexed-patterns)

    ;; sequence of the patterns sorted by increasing i
    patterns        (sort-by :i (vals patterns-by-i))

    ;; numbers that have a pattern - not all do - sorted in increasing value 
    pattern-numbers (sort (keys patterns-by-i))

    ;; countes of various values 
    pattern-counts (into {:total (count patterns)
                          :unsymmetrical (count (remove :symmetrical? patterns))}
                         (map (fn [f]
                                  [f (count (filter f patterns))])
                              [:symmetrical?
                               :symmetrical-h?
                               :symmetrical-v?
                               :symmetrical-r?
                               :symmetrical-h!!
                               :symmetrical-!v!
                               :symmetrical-!!r
                               :symmetrical-hv!
                               :symmetrical-h!r
                               :symmetrical-!vr
                               :symmetrical-hvr]))))


(comment

  (:pattern-counts patterns-3x5-8bit)

  (take 5 (:patterns patterns-3x5-8bit))

  (first (:patterns patterns-3x5-8bit))
  (map :pattern (filter :symmetrical-h? (:patterns patterns-3x5-8bit)))

  nil)
