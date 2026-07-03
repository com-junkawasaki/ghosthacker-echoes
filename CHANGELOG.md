# Changelog

pure `.cljc` 関係修復novel核（`ghosthacker-echoes.core`）と、それを使う
プロトタイプ実装の変更履歴（ADR-2607023200）。

## Unreleased

- 初期実装: `core.cljc`（dialogueグラフ駆動、`:connection`状態、
  `:healed`/`:fragile`/`:broken`のending判定）、`story.cljc`
  （サンプル会話`reach-out`、3結末全てに到達可能）、`terminal.clj`
  （プレイ可能な番号選択REPLプロトタイプ）。11 tests / 59 assertions。
  実プロセスとして手動検証済み（完走/EOF途中打ち切り/不正入力の再入力、
  いずれもハング無く正常終了）。
