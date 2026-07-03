(ns ghosthacker-echoes.story
  "GHOST HACKER: ECHOES — sample dialogue graph (pure data, ADR-2607023200).

  \"reach-out\": Nei messages an old contact she's fallen out of touch with,
  after noticing (her `:gh/observationEye` — the same perceptiveness
  ADR-2607023200 names for TUNING) that the silence between them has
  started to feel wrong. A short, complete conversation with three
  endings depending on how the choices land — data only, no logic; see
  `ghosthacker-echoes.core` for how a `dialogue` graph like this one is
  driven."
  (:require [ghosthacker-echoes.core :as core]))

(def reach-out
  "3手で終端に着く、小さな完結した会話。:start始点、:continues/:fadesが
   終端ノード(ノード名はあくまで雰囲気を示す名前——実際のエンディング階級
   (:healed/:fragile/:broken)はcore/endingが最終:connectionから判定する
   ので、必ずしもノード名と一致しない。6通りの完走パスで
   :healed×3/:fragile×2/:broken×1、3エンディング全てに到達可能な数値に
   調整済み。"
  {:start
   {:speaker :nei
    :text "（既読が付いたまま、もう三日経っている。）「……まだ、覚えてる?」"
    :choices
    [{:label :ask-directly :resonance 0.15 :next :direct-response}
     {:label :small-talk :resonance 0.0 :next :small-talk-response}
     {:label :apologize-first :resonance 0.1 :next :apology-response}]}

   :direct-response
   {:speaker :other
    :text "「……うん。忘れてないよ。ただ、どう返せばいいか分からなかった」"
    :choices
    [{:label :listen :resonance 0.2 :next :opens-up}
     {:label :push-for-answer :resonance -0.1 :next :fades}]}

   :small-talk-response
   {:speaker :other
    :text "「……そっちは元気? 特に用ってわけじゃないよね」"
    :choices
    [{:label :admit-it-matters :resonance 0.15 :next :opens-up}
     {:label :keep-it-light :resonance -0.1 :next :fades}]}

   :apology-response
   {:speaker :other
    :text "「謝るようなことじゃ……いや、正直ちょっとホッとした」"
    :choices
    [{:label :listen :resonance 0.2 :next :opens-up}
     {:label :change-subject :resonance -0.15 :next :fades}]}

   :opens-up
   {:speaker :other
    :text "「実はずっと、うまく言葉にできなくて。……聞いてくれる?」"
    :choices [{:label :stay :resonance 0.25 :next :continues}]}

   :fades
   {:speaker :other
    :text "「……うん、じゃあまた」"
    :choices [{:label :leave-it :resonance -0.05 :next :closes}]}

   :continues
   {:speaker :nei :text "（言葉になった沈黙の理由を、ちゃんと受け取った。）" :choices []}
   :closes
   {:speaker :nei :text "（既読だけが残って、会話はそこで止まっている。）" :choices []}})

(defn play-reach-out
  "reach-outをchoice-indicesで再生し、summaryを返す(core/play-summaryの薄い
   ラッパー)。"
  [choice-indices]
  (core/play-summary reach-out :start choice-indices))
