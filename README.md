# GHOST HACKER: ECHOES

![test](https://github.com/com-junkawasaki/ghosthacker-echoes/actions/workflows/test.yml/badge.svg)

Ghost Hacker ゲームポートフォリオ第3弾。設計は
[ADR-2607023200](../../../90-docs/adr/2607023200-ghosthacker-game-portfolio-flow.md)
（superproject `com-junkawasaki/root`）を参照。

[Ghost Hacker](https://github.com/com-junkawasaki/ghosthacker)（既存カノン: Ren/Nei、
「情報は物理だ」、情報場、Ghost Battle / Daemon Battle）を土台に、FreeTEMPOの
『The World Is Echoed』（2003）に由来する、10ジャンル展開の第3弾。

## コンセプト

- **ジャンル**: アドベンチャー（関係修復ノベル）
- **主人公**: Nei単独
- **コアループ**: 疎遠になった相手との会話を、選択肢で紡ぎ直す。各選択が
  `:connection`（0.0=完全に疎遠 〜 1.0=完全に通じ合っている）を上下させ、
  会話の終端に着いた時点の`:connection`が最終的な結末
  （`:healed`/`:fragile`/`:broken`）を決める。README原点の
  "Healing connections in a disconnected world" を、演出でなく選択の結果
  として体現する — FLOW/HARMONYの`:groove`crossfadeと同じ設計思想を、
  リズムではなく会話の領域に持ち込んだもの

## 実装範囲

`src/ghosthacker_echoes/core.cljc` — pure、host-free。dialogueグラフ
（`{node-id {:speaker :text :choices [{:label :resonance :next} ...]}}`、
選択肢が空/無いノードが終端）を駆動する:

- `choose` — 選択を適用し`:connection`を更新(clamp付き)、`:node`を進め、
  `:history`に選択ラベルを積む
- `ending` — 終端到達時点の`:connection`から結末を判定
  （`:healed` >= 0.75、`:fragile` >= 0.4、それ以外は`:broken`）
- `play`/`play-summary` — 選択列をまとめて適用し、リザルトを返す統合API

`src/ghosthacker_echoes/story.cljc` — サンプルの完結した会話
（`reach-out`）。3手で終端に着き、3つの結末全てに到達可能な数値に
調整済み（テストで検証）。

**プレイ可能な最小プロトタイプ**として `src/ghosthacker_echoes/terminal.clj`
がある。FLOW/HARMONYと違い実時間のビート判定が無いため、`future`/agent
スレッドプールを一切使わない素朴な番号選択REPLループ（`shutdown-agents`の
ハングリスクも無い）。不正な入力は再入力を促し、EOFなら未完走のまま
そこまでの結果を表示する。

本格的なレンダリング/入力ホストアダプタは依然として別レイヤーの課題。

## 開発

```bash
clojure -M:test
```

Lint（clj-kondo、Clojars経由でHomebrew等の別インストール不要）:

```bash
clojure -M:lint
```

遊んでみる:

```bash
clojure -M -m ghosthacker-echoes.terminal
```

変更履歴は [CHANGELOG.md](CHANGELOG.md)。
