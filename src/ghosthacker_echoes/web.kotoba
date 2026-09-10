(ns ghosthacker-echoes.web
  "GHOST HACKER: ECHOES -- browser host adapter (ADR-2607100900 follow-up
  (b): ClojureScript is the first-choice runtime for the game portfolio's
  new hosts (ADR-2607100100 order), since kotoba wasm's closed capability
  surface and clojurewasm's import-free-only FFI (ADR-2607100030 addendum
  2) can't host guests needing host-imports. ECHOES itself needs none of
  that -- no real-time beat, no audio, no low-latency input, just DOM
  render + click -- so this is the plain, low-risk end of the fallback,
  unlike FLOW/HARMONY's audio/timing hosts.

  Same input/output shape as ghosthacker-echoes.terminal (a click maps to
  the same 0-based choice index `terminal/parse-choice` derives from
  stdin), rendering to the DOM instead of stdout."
  (:require [kotoba.lang.text :as str]
            [reagent.core :as r]
            [reagent.dom :as rdom]
            [ghosthacker-echoes.core :as core]
            [ghosthacker-echoes.story :as story]))

(defonce state (r/atom (assoc core/initial-state :node :start)))

(defn- restart! [] (reset! state (assoc core/initial-state :node :start)))
(defn- choose! [idx] (swap! state #(core/choose story/reach-out % idx)))

(defn- ending-label [ending]
  (case ending
    :healed "healed"
    :fragile "fragile"
    :broken "broken"
    "(未完走)"))

(defn- speaker-line [node]
  [:p.echoes-line (str "[" (name (:speaker node)) "] " (:text node))])

(defn- result-view [s]
  (let [{:keys [ending connection history]} (core/summary story/reach-out s)]
    [:div.echoes-result
     [:h2 (str "ending: " (ending-label ending))]
     [:p (str "connection: " (.toFixed connection 2))]
     [:p.echoes-history (str "history: " (str/join " -> " (map name history)))]
     [:button {:on-click restart!} "もう一度"]]))

(defn- choices-view [s]
  (into [:div.echoes-choices]
        (map-indexed
         (fn [i {:keys [label]}]
           ^{:key i}
           [:button {:on-click #(choose! i)} (name label)])
         (core/choices-at story/reach-out s))))

(defn app []
  (let [s @state
        node (core/node story/reach-out (:node s))]
    [:div.echoes-app
     [:h1 "GHOST HACKER: ECHOES"]
     [:p.echoes-sub "reach-out"]
     (speaker-line node)
     (if (core/terminal? story/reach-out (:node s))
       [result-view s]
       [choices-view s])]))

(defn ^:export mount []
  (when-let [el (.getElementById js/document "app")]
    (rdom/render [app] el)))

(defn ^:export init [] (mount))
