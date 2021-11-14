(ns app.data.fns
 (:require
   [dsann.let-map :refer [let-map]]
   [clojure.pprint :refer [cl-format]]))

;; https://stackoverflow.com/questions/5057047/how-to-do-exponentiation-in-clojure
(defn exp [x n]
  (loop [acc 1 n n]
    (if (zero? n) acc
        (recur (* x acc) (dec n)))))

;; taken from here
;; https://metaljoe.wordpress.com/2010/08/12/bit-counts-in-python-erlang-and-clojure/
(defn bitcount
  "return the number of 1s in the binary representation of i"
  ([v]   (bitcount v 0))
  ([v n] (if (zero? v)
           n
           (recur (bit-and v (dec v)) (inc n)))))

(defn binary-str 
  "format i as a binary string"
  [i]
  (cl-format nil "~b" i))

(defn binary-vec [powers-of-2 i]
  "produce a vector of 0 and 1 as per the binary representation of i
    powers-of-2 is a seq of powers of 2 in descending order"
  (into [] (map (fn [x] (if (zero? (bit-and x i)) 0 1))
                powers-of-2)))

(defn flip-horizontal
  "flip pattern horizontally
    - a pattern is fliped horizontally if all rows are reversed"
  [pattern]
  (map reverse pattern))

(defn flip-vertical [pattern]
  "flip pattern vertically
    - a pattern is fliped vertically if all cols are reversed"
  (reverse pattern))

(defn index-by-pattern
  "create an index (map) from pattern to pattern data" 
  [patterns]
  (into {} (map (fn [p] [(:pattern p) p]))
           patterns))

(defn pattern-ids 
  "given an index by pattern, find the i value for the symmetrical patterns for p-data"
  [patterns-index p-data]
  (let-map
     pattern-h-i (:i (get patterns-index (:pattern-h p-data)))
     pattern-v-i (:i (get patterns-index (:pattern-v p-data)))
     pattern-r-i (:i (get patterns-index (:pattern-r p-data)))))

(defn link-indexed-patterns [indexed-patterns]
  "add cross referencing indices for all symmatrical patterns"
  (reduce
    (fn [r [p p-data]]
      (let [i (:i p-data)]
       (assoc r i (merge p-data
                         (pattern-ids indexed-patterns p-data)))))
    {}
    indexed-patterns))



(comment


  (:pattern-counts patterns-3x5-8bit)

  (take 5 (:patterns patterns-3x5-8bit))


  (first (:patterns patterns-3x5-8bit))
  (take 200 (map :pattern (filter :symmetrical-h? (:patterns patterns-3x5-8bit))))

  (def indexed-patterns (index-patterns patterns))
  (pattern-ids indexed-patterns (first patterns))

  (get patterns-index (:v-pattern p1))

  (first linked-patterns)
  (linked-patterns 27434)
  (linked-patterns-index 9118)

  (bitcount highest)
  (count (binary-str (dec (exp 2 15))))
  nil)
