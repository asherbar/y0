(ns y0.rules-test
  (:require [midje.sweet :refer [fact =>]]
            [y0.rules :refer [new-vars add-rule]]
            [y0.core :refer [all <-]]
            [y0.status :refer [->s ok let-s]]
            [y0.predstore :refer [match-rule pred-key]]))

;; This module is responsible for rules and their interpretation.

;; ## Variable Bindings

;; The term _variable binding_ refers to the matching between a symbol representing a variable
;; and the underlying variable (an `atom`). Variable bindings are represented programatically as
;; Clojure maps, with the symbols being keys and the atoms being the values.

;; In $y_0$, new variable bindings are introduced using vectors of symbols. Each symbol is
;; assigned a fresh variable (`(atom nil)`).

;; The function `new-vars` takes a binding and a vector of symbols and returns the binding updated
;; with the new fresh variables.
(fact
 (let [var-binding (new-vars {} '[foo bar baz])]
   (get var-binding 'foo) => #(instance? clojure.lang.Atom %)
   @(get var-binding 'foo) => nil?
   (get var-binding 'bar) => #(instance? clojure.lang.Atom %)
   @(get var-binding 'bar) => nil?
   (get var-binding 'baz) => #(instance? clojure.lang.Atom %)
   @(get var-binding 'baz) => nil?))

;; `new-vars` override any existing variables in the bindings.
(fact
 (let [var-binding (new-vars {'foo (atom 3)} '[foo])] 
   @(get var-binding 'foo) => nil?))

;; ## Rule Parsing

;; The function `add-rule` takes a predstore and an s-expression which represents a logic rule
;; and returns a status containing the predstore, with the rule added.

;; ### Trivial Rules

;; A trivial rule has the form: `(all [bindings...] goal)`. Its means that for all possible values
;; replacing the symbols in `bindings...`, `goal` is satisfied.

;; To make this more clear, let us work through an example. Let us define predicate `amount`,
;; which matches an "amount" to a given number. `0` is matched with `:zero`, `1` is matched with
;; `:one` and anything else is matched with `:many`.

;; We define this predicate through three rules, defined here `amount0`, `amount1` and
;; `amount-base`.
(def amount0 `(all [] (amount 0 :zero)))
(def amount1 `(all [] (amount 1 :one)))
(def amount-base `(all [x] (amount x :many)))

;; Note that in the first two rules, the list of bindings is empty, because the rule matches a
;; concrete value. The third matches any value, and so a binding of the symbol `x` is used.

;; Now we use `add-rule` three times to create a predstore consisting of this predicate. Note that
;; We add the base-rule first to comply with the [rules for defining rules](predstore.md#predicate-definitions).
;; The predstore should have rules to match goals of the form `(amount x y)`.
(fact
 (let-s [ps (->s (ok {})
                 (add-rule amount-base)
                 (add-rule amount0)
                 (add-rule amount1))
         x (ok nil atom)
         r0 (match-rule ps `(amount 0 ~x))
         r1 (match-rule ps `(amount 1 ~x))
         r2 (match-rule ps `(amount 2 ~x))]
        (do (def amount-ps ps)  ;; for future reference
            (def amount-r0 r0)
            (def amount-r1 r1)
            (def amount-r2 r2)))
 amount-r0 => fn?
 amount-r1 => fn?
 amount-r2 => fn?)

