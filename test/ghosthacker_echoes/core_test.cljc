(ns ghosthacker-echoes.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [ghosthacker-echoes.core :as core]))

(def sample-dialogue
  {:start {:speaker :a :text "hi"
           :choices [{:label :warm :resonance 0.2 :next :end}
                     {:label :cold :resonance -0.3 :next :end}]}
   :end {:speaker :a :text "bye" :choices []}})

(deftest clamp01-test
  (is (== 0.0 (core/clamp01 -0.5)))
  (is (== 1.0 (core/clamp01 1.5)))
  (is (== 0.5 (core/clamp01 0.5))))

(deftest node-and-terminal-test
  (is (= {:speaker :a :text "hi" :choices [{:label :warm :resonance 0.2 :next :end}
                                           {:label :cold :resonance -0.3 :next :end}]}
         (core/node sample-dialogue :start)))
  (is (false? (core/terminal? sample-dialogue :start)))
  (is (true? (core/terminal? sample-dialogue :end))))

(deftest choose-test
  (testing "resonanceがconnectionに足され、nodeが進み、historyに積まれる"
    (let [state (assoc core/initial-state :node :start)
          next-state (core/choose sample-dialogue state 0)]
      (is (== (+ core/initial-connection 0.2) (:connection next-state)))
      (is (= :end (:node next-state)))
      (is (= [:warm] (:history next-state)))))
  (testing "connectionは[0.0, 1.0]にclampされる"
    (let [state (assoc core/initial-state :node :start :connection 0.95)
          next-state (core/choose sample-dialogue state 0)]
      (is (== 1.0 (:connection next-state))))))

(deftest ending-test
  (is (= :healed (core/ending {:connection 0.75})))
  (is (= :healed (core/ending {:connection 1.0})))
  (is (= :fragile (core/ending {:connection 0.4})))
  (is (= :fragile (core/ending {:connection 0.74})))
  (is (= :broken (core/ending {:connection 0.39})))
  (is (= :broken (core/ending {:connection 0.0}))))

(deftest play-test
  (testing "終端に着くまでchoice-indicesを順に適用する"
    (let [state (core/play sample-dialogue :start [0])]
      (is (= :end (:node state)))
      (is (== (+ core/initial-connection 0.2) (:connection state)))))
  (testing "終端に着いた後の余った選択肢は無視される"
    (let [state (core/play sample-dialogue :start [0 0 0])]
      (is (= [:warm] (:history state)))))
  (testing "choice-indicesが尽きても終端に着いていなければ、そこまでのstateを返す"
    (let [state (core/play sample-dialogue :start [])]
      (is (= :start (:node state))))))

(deftest summary-and-play-summary-test
  (testing "終端に達していればendingを含む"
    (let [result (core/play-summary sample-dialogue :start [0])]
      (is (= :end (:node result)))
      (is (some? (:ending result)))))
  (testing "終端に達していなければendingはnil"
    (let [result (core/play-summary sample-dialogue :start [])]
      (is (nil? (:ending result))))))
