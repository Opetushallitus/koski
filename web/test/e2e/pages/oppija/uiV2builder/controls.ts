import { Page, Locator } from '@playwright/test'

export type Leaf<T = any> = {
  (locator: Locator): T
  isLeaf: true
}

export const isLeaf = (a: any): a is Leaf => a?.isLeaf

export function createLeaf<T>(build: (locator: Locator) => T): Leaf<T> {
  return Object.assign(build, { isLeaf: true }) as Leaf<T>
}

export type Button = Leaf<{
  click: () => Promise<void>
  innerText: () => Promise<string>
}>

export const Button: Button = createLeaf((locator) => ({
  click: () => locator.click(),
  innerText: () => locator.innerText()
}))

export type Label = Leaf<{
  innerText: () => Promise<string>
}>

export const Label: Label = createLeaf((locator) => ({
  innerText: () => locator.innerText()
}))
