import { Observable } from 'baconjs'
import { Lens } from 'partial.lenses'

declare function Atom<T>(t?: T): Atom<T>
export = Atom

interface Atom<T> extends Observable<T> {
  filter(f: (t: T) => t is NonNullable<T>): Atom<NonNullable<T>>
  filter(f: (t: T) => boolean): Atom<T>
  onValue(f: (t: T) => void): Atom<T>
  view<S>(l: Lens<T, S>): Atom<S>
  set(t: T): Atom<T>
  get(): T
}
