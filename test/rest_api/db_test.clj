(ns rest-api.db-test
  (:require [clojure.test :refer :all]
            [rest-api.db :refer :all]))


(def fixture-db-filename "db.json")
(def fixture-db-md5 "08504ad0ad285686a2bda1edd99beb65")    ;; md5 sum of the data on witch the tests are known to work

(def org-db-atom (atom {}))
(def test-db-atom (atom {}))

(defn test-fixture [test-runner]
    (println "Setup test fixture database")
    (reset! org-db-atom @db-atom)
    (init-db-atom-from-file test-db-atom fixture-db-filename)
    (reset! db-atom @test-db-atom)
    (test-runner)
    (println "Restore pre-test database")
    (reset! db-atom @test-db-atom))

(use-fixtures :once test-fixture)


(deftest db-integrity
  (testing "Test Data Integrity: Check db exists and matching expected md5 sum"
    (and (is (= @org-db-atom @test-db-atom))
         (is (= fixture-db-md5 (:db-md5 @test-db-atom))))))


(deftest db-parsing
  (testing "Data Extraction: Find all employees"
    (is (= 10 (count (find-all-employees (:db @test-db-atom))))))
  (testing "Data Extraction: Find all accounts"
    (is (= 9 (count (find-all-accounts (:db @test-db-atom)))))))


(deftest db-query
  (testing "Data Query: No data"
    (let [no-employees (search-employees {:search-string "john"})]
      (and (is (empty? (-> no-employees :items)))
           (is (= no-employees {:items [],
                                :items_total 0,
                                :next_page nil,
                                :page 1,
                                :pages 1,
                                :per_page 5,
                                :prev_page nil,
                                :search-term "john"})))))
  (testing "Data Query: Paged result and query parameters"
    (let [alta-result (search-employees {:search-string "@" :page "2" :per_page "3"})]
      (println "count = " (count (alta-result :items)))
      (and (is (= 3 (count (:items alta-result))))
           (is (= {:items_total 9,
                   :page        2,
                   :prev_page   1,
                   :next_page   3,
                   :pages       3,
                   :per_page    3,
                   :search-term "@"}
                  (dissoc alta-result :items))))))
  (testing "Data Query: Single result"
    (let [result (search-employees {:search-string "donald"})]
      (and (is (= {:items_total 1,
                   :page        1,
                   :prev_page   nil,
                   :next_page   nil,
                   :pages       1,
                   :per_page    5,
                   :search-term "donald"}
                  (dissoc result :items)))
           (is (= "171" (get (-> (get-in a [:items]) first) "id") )))))
  (testing "Data Query: Request out-of-bounds"
    (is (= {:items       []
            :items_total 9,
            :page        5,
            :prev_page   3,
            :next_page   nil,
            :pages       3,
            :per_page    3,
            :search-term "@"}
           (search-employees {:search-string "@" :page "5" :per_page "3"})
           ))))


