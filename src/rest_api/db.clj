(ns rest-api.db
  (:require [clojure.java.io :as io]
            [clojure.edn :as edn]
            [clojure.data.json :as json]
            [clojure.string :refer [lower-case]]
            [digest :refer [md5]]))

;; Data atom holding the db when loaded
(def db-atom (atom {}))

;; Strategy
;; ====================================================================================
;; json document consists of 4 keys ("data" "included" "meta" "links") where
;; only "data" and "included" contains relevant data. Extract, de-duplicate and
;; prepare employee data for efficient query.
;; All employees are gathered from data and included keys, combined with emails
;; linked in from account and saved under a fast-search field.
;;
;; init-db-atom-from-file() is where the action is at

(defn find-all-employees
  "Extract employee records from 'data' and 'included' keys, removing duplicates"
  [db]
  (distinct (flatten (map (fn [tag]
                            (filter (fn [record]
                                      (= (record "type") "employees")) (db tag)))
                          ["data" "included"]))))

(defn find-all-accounts
  "Extract account records from 'included' key"
  [db]
  (filter (fn [record] (= (record "type") "accounts")) (db "included")))


(defn create-db-cache
  "Link account email if any to the record attributes. Add :fast-search key to avoid
  matching in two lists when reg-ex'ing for search term later on"
  [all-accounts all-employees]
  (let [email-by-account-id (reduce (fn [m record]
                                      (into m {(record "id") (get-in record ["attributes" "email"])}))
                                    {}
                                    all-accounts)
        tagged-records (map (fn [record]
                              (let [account-id (get-in record ["relationships" "account" "data" "id"])
                                    email (email-by-account-id account-id)
                                    fast-search (lower-case (str (get-in record ["attributes" "name"])
                                                                 " "
                                                                 (if email email)))]
                                {:fast-search fast-search
                                 :record      (merge-with into record {"attributes" {"email" email}})}))
                            all-employees)]
    tagged-records))


(defn init-db-atom-from-file
  "Initialize db atom from filename. Expects a json file with the structure given in the code challenge data sample.

  The json is parsed and db atom is initialized with two fields:
  :db-md5    - with the md5 of the json file
  :db-cache  - a preprocessed version of the json db with extra field to enable more efficient searching
               across name and email fields.
  "
  [db-atom filename]
  (let [db-raw (slurp (io/resource filename))
        db (json/read-str db-raw)]
    (reset! db-atom {:db-md5   (md5 db-raw)
                     :db       db
                     :db-cache (create-db-cache (find-all-accounts db) (find-all-employees db))})))

(init-db-atom-from-file db-atom "db.json")


(defn parse-pos-int [str default]
  (try (Integer/parseInt str)
       (catch Exception e default)))


(defn search-employees
  "Search employees for occurrence of search-string in name or email.

  Additionally attempts to respect pagination hints provided.
  Options field is expected to contain:
    * search-string - the string to filter employees by (string). If this is omitted nothing is returned
    * page-size - requested page size (int)
    * page - requested page number (0 < page <= page-size (int)"
  [{:keys [search-string per_page page] :as options}]

  (let [regex-str (if (and search-string
                           (> (count search-string) 0))     ;; exceptional case - nothing is asked for return empty
                    (str "(.*)" (clojure.string/lower-case search-string) "(.*)") ;; generic case we search for something
                    "$a")
        pattern (re-pattern regex-str)
        matches (filter #(re-matches pattern (% :fast-search)) (:db-cache @db-atom))
        page-size (parse-pos-int per_page 5)
        pages (partition-all page-size (map :record matches))
        num-pages (max 1 (count pages))
        cur-page (parse-pos-int page 1)]

    {:items       (if (and (not-empty pages)
                           (>= num-pages cur-page))
                    (nth pages (- cur-page 1))
                    [])
     :items_total (count matches)

     :page        cur-page
     :prev_page   (if (or (> 1 (- cur-page 1))
                          (empty? matches))
                    nil
                    (min (- cur-page 1) num-pages))
     :next_page   (if (>= num-pages (+ cur-page 1))
                    (+ cur-page 1)
                    nil)
     :pages       num-pages
     :per_page    page-size
     :search-term search-string}))
;:pattern     pattern
;:options     options



