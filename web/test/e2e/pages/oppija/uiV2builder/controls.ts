import { Locator } from '@playwright/test'

export type Control<T = any> = {
  (locator: Locator, child: (postfix: string) => Locator, testId: string): T
  isLeaf: true
}

export const isLeaf = (a: any): a is Control => a?.isLeaf

export function createControl<T>(
  build: (
    locator: Locator,
    child: (postfix: string) => Locator,
    testId: string
  ) => T
): Control<T> {
  return Object.assign(build, { isLeaf: true }) as Control<T>
}

export type Viewer = {
  value: () => Promise<string>
}

export type Editor = {
  value: () => Promise<string>
  set: (value: string) => Promise<void>
}

export const FormField = <E extends Editor>(
  viewer: Control<Viewer>,
  editor?: Control<E>
) =>
  createControl((_self, child, id) => {
    const view = viewer(
      child('value'),
      (postfix) => child(`value.${postfix}`),
      id
    )
    const edit = editor?.(
      child('edit'),
      (postfix) => child(`edit.${postfix}`),
      id
    )
    return {
      ...edit,
      value: (editMode: boolean) =>
        editMode && edit ? edit.value() : view.value(),
      set: (value: string) => {
        if (!edit) {
          throw new Error('Trying to set an editor value without editor')
        }
        return edit.set(value)
      }
    }
  }) as Control<
    E & {
      value: (editMode: boolean) => Promise<string>
      set: (value: string) => Promise<void>
    }
  >
