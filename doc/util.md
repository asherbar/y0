* [Utility Predicates](#utility-predicates)
  * [Collection Predicates](#collection-predicates)
    * [Abstract Collections](#abstract-collections)
    * [Verifying Elements](#verifying-elements)
```clojure
(ns util)

```
# Utility Predicates

This module defines generic utility predicates, useful for defining
languages.

## Collection Predicates

The predicates defined in this section operate on lists and vectors,
commonly referred to as _collections_.

### Abstract Collections

The following predicates provide an abstraction layer on top of lists and
vectors, allowing calling definitions to be agnostic of their underlying
type.

`empty?` matches a collection with a Boolean, determining if it's empty.
The default rule will just return `false`.
```clojure
(all [coll]
     (empty? coll false))

```
Special cases for an empty vector and an empty list return `true`.
```clojure
(all []
     (empty? [] true))
(all []
     (empty? () true))

(assert
 (empty? [] true)
 (empty? () true)
 (empty? [1] false)
 (empty? (2) false)
 (empty? :not-a-collection false))

```
`decons` deconstructs a collection by returning its first element (head) and
the remaining collection (tail).

It fails for all non-collections
```clojure
(all [coll head tail]
     (decons coll head tail
             ! coll "is not a collection"))

```
Non-empty lists and vectors are handled as follows:
```clojure
(all [x xs]
     (decons [x & xs] x xs))
(all [x xs]
     (decons (x & xs) x xs))

```
A special explanation is given for the failed case of empty lists and
vectors.
```clojure
(all [x xs]
     (decons [] x xs ! "Attempt to deconstruct an empty vector"))
(all [x xs]
     (decons () x xs ! "Attempt to deconstruct an empty list"))

(assert
 (decons [1 2 3] 1 [2 3])
 (decons (1 2 3) 1 (2 3))
 (decons [1] 1 [])
 (exist [x xs]
        (decons [] x xs) ! "Attempt to deconstruct an empty vector")
 (exist [x xs]
        (decons () x xs) ! "Attempt to deconstruct an empty list"))

```
### Verifying Elements

Here we define two predicates that verify that the elements of a collection
(vector or list) all meet a certain criterion. In order to be able to do
this we need to first define an abstraction for defining predicates on the
elements. To do so, we define `is`, a predicate that takes a `description`
and a term and accepts the term if it matches the description.

By default, `is` will reject the description.
```clojure
(all [desc term]
     (is desc term ! desc "is not a valid term description"))

```
A description of `symbol` requires that the term is a symbol.
```clojure
(all [term]
     (is symbol term) <-
     (inspect term :symbol ! "Expected a symbol, received" term))

(assert
 (is symbol foo)
 (is symbol 42 ! "Expected a symbol, received" 42)
 (is not-a-description foo
     ! not-a-description "is not a valid term description"))

```
`vector-of` is a predicate that takes a term expected to be a vector and a
description for its elements. It accepts vectors where all elements match
the description.
```clojure
(all [vec desc]
     (vector-of vec desc ! vec "is not a vector"))
(all [x xs desc]
     (vector-of [x & xs] desc) <-
     (is desc x)
     (vector-of xs desc))
(all [desc]
     (vector-of [] desc))

(assert
 (vector-of [a b c] symbol)
 (vector-of [a 3 c] symbol ! "Expected a symbol, received" 3)
 (vector-of (a b c) symbol ! (a b c) "is not a vector"))

```
Similarly, `list-of` accepts a list of element that match the given
description.
```clojure
(all [list desc]
     (list-of list desc ! list "is not a list"))
(all [x xs desc]
     (list-of (x & xs) desc) <-
     (is desc x)
     (list-of xs desc))
(all [desc]
     (list-of () desc))

(assert
 (list-of (a b c) symbol)
 (list-of (a 3 c) symbol ! "Expected a symbol, received" 3)
 (list-of [a b c] symbol ! [a b c] "is not a list"))
```
