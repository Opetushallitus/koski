declare module "baret" {
  import * as React from "react";
  export = React;
}

declare module "bacon.atom" {
  import { Observable } from "baconjs";
  import { Lens } from "partial.lenses";

  function Atom<T>(t?: T): Atom<T>;
  export = Atom;

  interface Atom<T> extends Observable<T> {
    filter(f: (t: T) => t is NonNullable<T>): Atom<NonNullable<T>>;
    filter(f: (t: T) => boolean): Atom<T>;
    onValue(f: (t: T) => void): Atom<T>;
    view<S>(l: Lens<T, S>): Atom<S>;
    set(t: T): Atom<T>;
    get(): T;
  }
}

declare module "baconjs" {
  function Bus<T, I>(): Bus<T, I>;

  declare type EventSink<V> = (event: Event<V>) => Reply;
  declare type Function0<R> = () => R;
  declare type Function1<T1, R> = (t1: T1) => R;
  declare type Function2<T1, T2, R> = (t1: T1, t2: T2) => R;
  declare type Function3<T1, T2, T3, R> = (t1: T1, t2: T2, t3: T3) => R;
  declare type Function4<T1, T2, T3, T4, R> = (t1: T1, t2: T2, t3: T3, t4: T4) => R;
  declare type Function5<T1, T2, T3, T4, T5, R> = (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5) => R;
  declare type Function6<T1, T2, T3, T4, T5, T6, R> = (t1: T1, t2: T2, t3: T3, t4: T4, t5: T5, t6: T6) => R;

  function constant<T>(t: T): Observable<T>;

  class Observable<T> {
    map<S>(f: (a: T) => S): Observable<S>;
    map<S>(p: string): Observable<S>;
    map(e: JSX.Element): JSX.Element // baret
    not(): Observable<boolean>;
    log(text: string): Observable<T>;
    doAction(f: Function1<V, any>): this
    toProperty(): Property<T>
  }

  interface EventStreamOptions {
    forceAsync: boolean;
  }

  class Property<V> extends Observable<V> {
    constructor(desc: Desc, subscribe: Subscribe<V>, handler?: EventSink<V>);
    and(other: Property<any>): Property<boolean>;
    changes(): EventStream<V>;
    concat(other: Observable<V>): Property<V>;
    concat<V2>(other: Oversable<V2>): Property<V | V2>;
    map<V2>(f: Function1<V, V2>): Property<V2>;
    map<V2>(f: Property<V2> | V2): Property<V2>;
    not(): Property<boolean>
    or(other: Property<any>): Property<boolean>
    sample(interval: number): EventStream<V>
    toEventStream(options?: EventStreamOptions): EventStream<V>
    toProperty(): Property<V>
    startWith(seed: V): Property<V>;
  }

  interface EventStream<T> extends Observable<T> {}

  interface Bus<T, I> extends EventStream<T> {
    push(t: I): Bus<T, I>;
    end(): Bus<T, I>;
    error(e: any): Bus<T, I>;
    scan<S>(seed: S, f: (a: S, b: I) => S): Bus<S, I>;
  }
}

declare module "partial.lenses" {
  type Lens<T, S> = { _tag: "Lens" } | string;
  type Traversal<T, S> =
    | string
    | { _tag: "Traversal" }
    | Traversal<T, S>[]
    | Lens<T, S>;
  type Optic<T, S> = Traversal<T, S>;

  function index<T, S>(i: number): Lens<T, S>;
  function lens<T, S>(
    getter: (maybeData?: T, i?: number) => S | undefined,
    setter: (maybeValue?: S, maybeData?: T, i?: number) => T | undefined
  ): Lens<T, S>;
  function prop<T>(name: string): Lens<object, T>;
  function compose<T, S>(...lenses: Lens<T, S>[]): Lens<T, S>;
  function get<T, S>(traversal: Traversal<T, S>, maybeData?: T): S | undefined;
  function set<T, S>(
    optic: Optic<T, S>,
    maybeValue?: S,
    maybeData?: T
  ): T | undefined;
  function find<T>(
    condition: (maybeValue?: T, index?: number) => boolean
  ): Lens<Array<T>, T>;
}
