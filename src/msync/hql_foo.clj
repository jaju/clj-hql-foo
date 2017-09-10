(ns msync.hql-foo
  (:require [clojure.walk :as w]
            [clojure.core :as cc]
            [cheshire.core :as json]
            [clojure.set :as set]
            [clojure.string :as s])
  (:refer-clojure :exclude [seq tree-seq])
  (:import [org.apache.hadoop.hive.ql.parse ASTNode HiveParser ParseDriver]
           [org.apache.hadoop.hive.ql.lib Node])
  (:gen-class))


(defn split-lines [s]
  (s/split s #"\n"))

(defn is-comment? [s]
  (.startsWith s "--"))

(defn semicolon-ending? [s]
  (.endsWith s ";"))

(defn trim-ending-semicolons [s]
  (if (semicolon-ending? s)
    (.substring s 0 (dec (.length s)))
    s))

(defn replace-$-placeholders [s]
  (s/replace s #"\$\{[^\{\}]*\}" "{dummy}"))

(defn grouping-reducer [[groups acc] next-vall]
  (let [next-val (if (seq? next-vall) (first next-vall) next-vall)]
    (if (semicolon-ending? next-val)
      [(conj groups (str acc " " (trim-ending-semicolons next-val))) ""]
      [groups (str acc " " next-val)])))

(defn ast-node-branch? [n] (pos? (.getChildCount n)))
(defn children [n] (.getChildren n))

(defn tree-seq [parsed]
  (cc/tree-seq ast-node-branch? children parsed))

(def tok-tab "TOK_TAB")
(def tok-tabref "TOK_TABREF")
(def tok-tabname "TOK_TABNAME")
(def tok-createtable "TOK_CREATETABLE")
(def tok-insertinto "TOK_INSERT_INTO")

(defn get-matching [l node-type]
  (filter #(= node-type (str %)) l))

(defn token-text [node]
  (-> node
      (.getToken)
      (.getText)))

(defn get-children [node]
  (let [num-children (.getChildCount node)]
    (map
      #(.getChild node %)
      (range num-children))))

(defn regenerate-table-name [table-parent-node]
  (let [children       (get-children table-parent-node)
        children-names (map token-text children)]
    (s/join "." children-names)))

(defn get-table-names [node]
  (let [t           (tree-seq node)
        table-nodes (get-matching t tok-tabname)]
    (map #(regenerate-table-name %) table-nodes)))

(defn get-unique-tables [nodes]
  (->> (map get-table-names nodes)
       flatten
       (into #{})))

(defn get-create-table [t]
  (let [nodes (get-matching t tok-createtable)]
    (if-not (empty? nodes)
      (-> nodes
          first
          (.getChild 0)
          list)
      [])))

(defn scan-tables [parsed]
  (let [t                  (tree-seq parsed)
        create-table-nodes (get-create-table t)
        insert-table-nodes (get-matching t tok-insertinto)
        tabref-nodes       (get-matching t tok-tabref)

        create-tables      (get-unique-tables create-table-nodes)
        insert-tables      (get-unique-tables insert-table-nodes)
        tabrefs            (get-unique-tables tabref-nodes)]
    {:create create-tables
     :insert (set/difference insert-tables create-tables)
     :refer  (set/difference tabrefs insert-tables create-tables)}))

(defn doit [query-str]
  (try
    (let [pd                  (ParseDriver.)
          sanitized-query-str (replace-$-placeholders query-str)
          parsed-query        (.parse pd sanitized-query-str)]
      (scan-tables parsed-query))
    (catch Exception e
      {:query query-str :error "Invalid query"})))

(defn construct-query-expressions-from-grouped-lines [grouped-lines]
  (let []
    (->> grouped-lines
         (reduce grouping-reducer [[] ""])
         first)))

(defn get-query-expressions [file-path]
  (let [file-content          (slurp file-path)
        lines                 (split-lines file-content)
        comment-removed-lines (filter (complement is-comment?) lines)
        grouped-lines         (partition-by semicolon-ending? comment-removed-lines)
        grouped-lines         (flatten grouped-lines)
        query-expressions     (construct-query-expressions-from-grouped-lines grouped-lines)]
    query-expressions))

(defn process-file [file-path]
  (let [statements (get-query-expressions file-path)]
    (map doit statements)))

(defn -main [& args]
  (doseq [file-name args]
    (println (str "File: " file-name))
    (println (process-file file-name))
    (println)))
