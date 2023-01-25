declare module 'baret' {
  import * as React from 'react'
  export = React
}

declare module 'bacon.atom' {
  import { Observable } from 'baconjs'
  import { Lens } from 'partial.lenses'

  function Atom<T>(t?: T): Atom<T>
  export = Atom

  interface Atom<T> extends Observable<T> {
    filter(f: (t: T) => t is NonNullable<T>): Atom<NonNullable<T>>
    filter(f: (t: T) => boolean): Atom<T>
    onValue(f: (t: T) => void): Atom<T>
    view<S>(l: Lens<T, S>): Atom<S>
    set(t: T): Atom<T>
    get(): T
  }
}

declare module 'baconjs' {
  function Bus<T, I>(): Bus<T, I>

  function constant<T>(t: T): Observable<T>

  class Observable<T> {
    map<S>(f: (a: T) => S): Observable<S>
    map<S>(p: string): Observable<S>
    map(e: JSX.Element): JSX.Element // baret
    not(): Observable<boolean>
    log(text: string): Observable<T>
  }

  type EventStream<T> = Observable<T>

  interface Bus<T, I> extends EventStream<T> {
    push(t: I): Bus<T, I>
    end(): Bus<T, I>
    error(e: any): Bus<T, I>
    scan<S>(seed: S, f: (a: S, b: I) => S): Bus<S, I>
  }
}

declare module 'partial.lenses' {
  type Lens<T, S> = { _tag: 'Lens' } | string
  type Traversal<T, S> =
    | string
    | { _tag: 'Traversal' }
    | Traversal<T, S>[]
    | Lens<T, S>
  type Optic<T, S> = Traversal<T, S>

  function index<T, S>(i: number): Lens<T, S>
  function lens<T, S>(
    getter: (maybeData?: T, i?: number) => S | undefined,
    setter: (maybeValue?: S, maybeData?: T, i?: number) => T | undefined
  ): Lens<T, S>
  function prop<T>(name: string): Lens<object, T>
  function compose<T, S>(...lenses: Lens<T, S>[]): Lens<T, S>
  function get<T, S>(traversal: Traversal<T, S>, maybeData?: T): S | undefined
  function set<T, S>(
    optic: Optic<T, S>,
    maybeValue?: S,
    maybeData?: T
  ): T | undefined
  function find<T>(
    condition: (maybeValue?: T, index?: number) => boolean
  ): Lens<Array<T>, T>
}
