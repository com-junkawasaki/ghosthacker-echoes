(ns ghosthacker-echoes.core
  "GHOST HACKER: ECHOES — relationship-repair adventure core (ADR-2607023200).

  Pure, host-free dialogue-tree/state engine for the ECHOES adventure loop:
  Nei navigates a branching conversation with someone the connection has
  gone quiet with. Each choice either echoes back (the connection
  resonates, `:connection` rises) or falls flat (silence, `:connection`
  falls); reaching a terminal dialogue node with the final `:connection`
  level decides the ending (`:healed` / `:fragile` / `:broken`) — thematizing
  the README's origin phrase, \"Healing connections in a disconnected
  world\", as something the player's choices actually determine rather than
  narration alone. No rendering, input, or text-display I/O lives here —
  those are host adapters layered on top, mirroring the
  ghosthacker.resources (pure) / ghosthacker.import (host) split already
  used elsewhere in this org, and the same core/host-adapter split
  ghosthacker-groove-core established for the rhythm titles.

  ## Dialogue graph shape

  A `dialogue` is `{node-id {:speaker str :text str :choices [choice ...]}}`.
  A node with an empty/absent `:choices` is terminal (the conversation ends
  there). A `choice` is `{:label str :resonance double :next node-id}` —
  `:resonance` is the signed `:connection` delta this choice applies
  (positive = it lands, negative = it falls flat)."
  )

(def initial-connection
  "既定の初期`:connection`(0.0=完全に疎遠 .. 1.0=完全に通じ合っている)。
   最初から『壊れている』でも『通じている』でもない、揺れている状態から
   始める。"
  0.5)

(def initial-state
  "会話開始時点のstate。:nodeは呼び出し側のdialogueグラフの開始ノード名
   （例: :start）を別途assocして使う想定——このnsはdialogueグラフの形を
   決めない。"
  {:connection initial-connection
   :node :start
   :history []})

(defn clamp01
  "xを[0.0, 1.0]に収める。"
  [x]
  (max 0.0 (min 1.0 x)))

(defn node
  "dialogueグラフからnode-idのノードを引く。"
  [dialogue node-id]
  (get dialogue node-id))

(defn terminal?
  "dialogue中のnode-idが終端ノード(選択肢が無い=会話がそこで終わる)か。"
  [dialogue node-id]
  (empty? (:choices (node dialogue node-id))))

(defn choices-at
  "dialogue中の現在ノードの選択肢一覧を返す(ホストアダプタの選択肢表示用)。
   終端ノードなら空のシーケンス。"
  [dialogue state]
  (:choices (node dialogue (:node state))))

(defn choose
  "現在ノードのchoice-idx番目の選択肢を適用したstateを返す:
   :connectionに:resonanceを足してclamp、:nodeを:nextへ進め、
   選択の:labelを:historyへ積む。"
  [dialogue state choice-idx]
  (let [choice (nth (choices-at dialogue state) choice-idx)]
    (-> state
        (update :connection #(clamp01 (+ % (:resonance choice))))
        (assoc :node (:next choice))
        (update :history conj (:label choice)))))

(def ending-thresholds
  "エンディング判定の閾値。:connectionがこの値以上でそのエンディング。
   いずれの閾値にも届かなければ:broken。"
  {:healed 0.75
   :fragile 0.4})

(defn ending
  "state（会話終了時点、:nodeが終端であることは呼び出し側の責任）の
   :connectionから、エンディングを返す(:healed / :fragile / :broken)。"
  [state]
  (let [c (:connection state)]
    (cond
      (>= c (:healed ending-thresholds)) :healed
      (>= c (:fragile ending-thresholds)) :fragile
      :else :broken)))

(defn play
  "start-nodeから開始し、choice-indicesを順にchooseで適用する。終端ノード
   に着いたら（choice-indicesが余っていても）そこで打ち切る。
   choice-indicesが尽きても終端に着いていなければ、そこまでのstateを返す
   （ホストアダプタが対話継続中に打ち切った場合に相当）。"
  [dialogue start-node choice-indices]
  (loop [state (assoc initial-state :node start-node)
         indices (seq choice-indices)]
    (if (terminal? dialogue (:node state))
      state
      (if-let [idx (first indices)]
        (recur (choose dialogue state idx) (next indices))
        state))))

(defn summary
  "state（dialogueグラフに対する）からリザルト画面向けのサマリを返す。
   終端ノードに達していなければ:endingはnil(未完走)。"
  [dialogue state]
  {:connection (:connection state)
   :node (:node state)
   :ending (when (terminal? dialogue (:node state)) (ending state))
   :history (:history state)})

(defn play-summary
  "play + summaryの合成。ホストアダプタが1本の会話ぶんの選択を録り終えた
   後に呼ぶ最短経路。"
  [dialogue start-node choice-indices]
  (summary dialogue (play dialogue start-node choice-indices)))
