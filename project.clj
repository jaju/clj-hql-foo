(defproject msync.hql-foo "0.1.0-SNAPSHOT"
  :description "A small utility for (shallow) analysis of HQLs"
  :url "https://github.com/jaju/clj-hql-foo"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.apache.hive/hive-exec "2.3.2"]
                 [cheshire "5.8.0"]]

  :main msync.hql-foo
  :manifest {"Main-Class" "msync.hql_foo"}
  :aot :all
  )
