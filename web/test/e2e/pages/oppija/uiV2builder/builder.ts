import { Page } from '@playwright/test'
import { nonNull } from '../../../../../app/util/fp/arrays'
import { mapObjectValues } from '../../../../../app/util/fp/objects'
import { isLeaf, Control } from './controls'

export type IdNode =
  | Control
  | IdNodeObject<string>
  | DynamicNumberIdNode<any>
  | DynamicStringIdNode<any>
  | ComposedFieldNode<any>
export type IdNodeObject<K extends string> = { [_ in K]: IdNode }
export type DynamicNumberIdNode<T extends IdNode> = (index: number) => T
export type DynamicStringIdNode<T extends IdNode> = (key: string) => T
export type ComposedFieldNode<T extends IdNode> = (editMode: boolean) => T

export type BuiltIdNode<T extends IdNode> = T extends Control<infer S>
  ? S
  : T extends DynamicNumberIdNode<infer S>
  ? (index: number) => BuiltIdNode<S>
  : T extends DynamicStringIdNode<infer S>
  ? (key: string) => BuiltIdNode<S>
  : T extends IdNodeObject<infer K>
  ? { [A in K]: BuiltIdNode<T[A]> }
  : never

export function build<T extends { [K in string]: IdNode }>(
  page: Page,
  obj: T,
  prefix?: string
): BuiltIdNode<T>

export function build<T extends Control>(
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
  fn: (index: string) => T,
  prefix?: string
): (index: string) => BuiltIdNode<T>

export function build(page: Page, node: IdNode, prefix?: string): any {
  if (isLeaf(node)) {
    if (!prefix) {
      throw new Error('Leaf cannot be the root of the hierarchy')
    }
    return node(
      page.getByTestId(prefix),
      (postfix) => page.getByTestId(`${prefix}.${postfix}`),
      prefix
    )
  }
  if (typeof node === 'object') {
    return mapObjectValues((value, key) =>
      build(page, value, mergeId(prefix, key))
    )(node)
  }
  if (typeof node === 'function') {
    return (index: number | string | boolean) => {
      return build(
        page,
        // @ts-ignore
        node(index),
        mergeId(
          prefix,
          typeof index === 'boolean'
            ? index
              ? 'edit'
              : 'view'
            : index.toString()
        )
      )
    }
  }
  throw new Error('Jokin meni pieleen')
}

export const arrayOf =
  <T extends IdNode>(node: T) =>
  (_index: number): T =>
    node

export const objectOf =
  <T extends IdNode>(node: T) =>
  (_key: string): T =>
    node

export const composedField =
  <T extends IdNode>(node: T) =>
  (_editMode: boolean): T =>
    node

const mergeId = (...tokens: Array<string | undefined>): string =>
  tokens.filter(nonNull).join('.')
