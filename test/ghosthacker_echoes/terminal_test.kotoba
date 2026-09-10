(ns ghosthacker-echoes.terminal-test
  "-mainそのものはテストせず(標準入出力をそのまま使うため)、private var
   経由でplay-loop!/read-valid-choice!/parse-choiceを直接叩く。実プロセス
   としての-main自体は手動検証済み(完走/EOF途中打ち切り/不正入力の再入力
   要求、いずれも正しく完了しプロセスがハングしないことを確認)。"
  (:require [clojure.test :refer [deftest is testing]]
            [ghosthacker-echoes.core :as core]
            [ghosthacker-echoes.story :as story]
            [ghosthacker-echoes.terminal :as terminal]))

(def ^:private parse-choice #'terminal/parse-choice)
(def ^:private read-valid-choice! #'terminal/read-valid-choice!)
(def ^:private play-loop! #'terminal/play-loop!)

(defn- silently [thunk]
  ;; swallow stdout while still returning thunk's value; with-out-str is
  ;; portable (JVM + ClojureScript), so no java.io.StringWriter import.
  (let [ret (promise)]
    (with-out-str (deliver ret (thunk)))
    @ret))

(deftest parse-choice-invalid-input-test
  (testing "範囲内の数字は0始まりindexを返す"
    (is (= 0 (parse-choice "1" 3)))
    (is (= 2 (parse-choice "3" 3))))
  (testing "範囲外/数字でない文字列はnil"
    (is (nil? (parse-choice "0" 3)))
    (is (nil? (parse-choice "4" 3)))
    (is (nil? (parse-choice "abc" 3))))
  (testing "前後の空白は無視する"
    (is (= 0 (parse-choice "  1  " 3)))))

(deftest read-valid-choice-invalid-input-boundary-test
  (testing "有効な入力を最初の行で読めば即座にそのindexを返す"
    (let [idx (silently #(with-in-str "2\n" (read-valid-choice! 3)))]
      (is (= 1 idx))))
  (testing "無効な入力は読み飛ばし、次の有効な行を返す"
    (let [idx (silently #(with-in-str "9\nabc\n1\n" (read-valid-choice! 3)))]
      (is (= 0 idx))))
  (testing "EOFに達したらnilを返す(ハングしない)"
    (is (nil? (silently #(with-in-str "" (read-valid-choice! 3)))))))

(deftest play-loop-eof-boundary-test
  (testing "有効な選択列を最後まで入力すれば終端nodeに着く"
    (let [state (silently #(with-in-str "1\n1\n1\n" (play-loop! story/reach-out :start)))]
      (is (core/terminal? story/reach-out (:node state)))
      (is (= :continues (:node state)))))
  (testing "途中でEOFになれば、そこまでのstateで打ち切る(未完走)"
    (let [state (silently #(with-in-str "1\n" (play-loop! story/reach-out :start)))]
      (is (false? (core/terminal? story/reach-out (:node state))))
      (is (= [:ask-directly] (:history state))))))
