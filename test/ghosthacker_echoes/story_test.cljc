(ns ghosthacker-echoes.story-test
  (:require [clojure.test :refer [deftest is testing]]
            [ghosthacker-echoes.core :as core]
            [ghosthacker-echoes.story :as story]))

(defn- close? [a b]
  (< (Math/abs (- (double a) (double b))) 1e-9))

(deftest reach-out-all-three-endings-are-reachable-test
  (testing ":healed — ask-directly -> listen -> stay"
    (let [result (story/play-reach-out [0 0 0])]
      (is (= :healed (:ending result)))
      (is (= :continues (:node result)))
      (is (close? 1.0 (:connection result)))))
  (testing ":fragile — ask-directly -> push-for-answer -> leave-it"
    (let [result (story/play-reach-out [0 1 0])]
      (is (= :fragile (:ending result)))
      (is (= :closes (:node result)))
      (is (close? 0.5 (:connection result)))))
  (testing ":broken — small-talk -> keep-it-light -> leave-it"
    (let [result (story/play-reach-out [1 1 0])]
      (is (= :broken (:ending result)))
      (is (= :closes (:node result)))
      (is (close? 0.35 (:connection result))))))

(deftest reach-out-is-a-valid-dialogue-graph-test
  (testing "全ノードから到達可能な:nextは実在するノードを指す"
    (doseq [[node-id {:keys [choices]}] story/reach-out
            {:keys [next]} choices]
      (is (contains? story/reach-out next)
          (str node-id " -> " next " は存在しないノードを指している"))))
  (testing ":startは非終端、終端ノードは:choicesが空"
    (is (false? (core/terminal? story/reach-out :start)))
    (is (true? (core/terminal? story/reach-out :continues)))
    (is (true? (core/terminal? story/reach-out :closes)))))
