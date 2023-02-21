import { Page } from '@playwright/test'
import { nonNull } from '../../../../../app/util/fp/arrays'
import { mapObjectValues } from '../../../../../app/util/fp/objects'
import { Button, isLeaf, Leaf } from './controls'

export type IdNode = Leaf | IdNodeObject<string> | DynamicIdNode<any>
export type IdNodeObject<K extends string> = { [_ in K]: IdNode }
export type DynamicIdNode<T extends IdNode> = (index: number) => T

export type BuiltIdNode<T extends IdNode> = T extends Leaf<infer S>
  ? S
  : T extends DynamicIdNode<infer S>
  ? (index: number) => BuiltIdNode<S>
  : T extends IdNodeObject<infer K>
  ? { [A in K]: BuiltIdNode<T[A]> }
  : never

type Foo = BuiltIdNode<{ asdasd: (i: number) => { lol: Button } }>
type Bar = BuiltIdNode<(i: number) => Button>

export function build<T extends { [K in string]: IdNode }>(
  page: Page,
  obj: T,
  prefix?: string
): BuiltIdNode<T>

export function build<T extends Leaf>(
  page: Page,
  leaf: T,
  prefix?: string
): BuiltIdNode<T>

export function build<T extends IdNode>(
  page: Page,
  fn: (index: number) => T,
  prefix?: string
): (index: number) => BuiltIdNode<T>

export function build<T extends IdNode>(
  page: Page,
  node: IdNode,
  prefix?: string
): any {
  if (isLeaf(node)) {
    return node(page.getByTestId(prefix || ''))
  }
  if (typeof node === 'object') {
    return mapObjectValues((value, key) =>
      build(page, value, mergeId(prefix, key))
    )(node)
  }
  if (typeof node === 'function') {
    return (index: number) => {
      return build(
        page,
        // @ts-ignore
        node(index),
        mergeId(prefix, index.toString())
      )
    }
  }
  throw new Error('Jokin meni pieleen')
}

export const arrayOf =
  <T extends IdNode>(node: T) =>
  (index: number): T =>
    node

const mergeId = (...tokens: Array<string | undefined>): string =>
  tokens.filter(nonNull).join('.')
