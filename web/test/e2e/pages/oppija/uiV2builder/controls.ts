import { Locator } from '@playwright/test'
import { expect } from '../../../base'

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
  createControl((self, child, id) => {
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

export const Button = createControl((self) => ({
  click: () => self.click(),
  value: () => self.innerText(),
  isVisible: () => self.isVisible(),
  isDisabled: () => self.isDisabled()
}))

export const Text = createControl((self) => ({
  value: () => self.innerText()
}))

export const Input = createControl((self, child) => ({
  click: () => self.click(),
  value: () => child('input').inputValue(),
  set: async (value: string) => {
    const input = child('input')
    console.log(
      'Input::set',
      input,
      'vs opiskeluoikeus.tila.edit.modal.date.edit.input'
    )
    await input.clear()
    await input.type(value)
  }
}))

export const NumberInput = createControl((self, child) => {
  return {
    click: () => self.click(),
    value: () => child('input').inputValue(),
    set: async (value: string) => {
      const input = child('input')
      await input.clear()
      await input.type(value)
    }
  }
})

export const Select = createControl((self, child) => {
  const showOptions = () => child('input').click()

  return {
    set: async (key: string) => {
      console.log('set', key, child('input'))
      await showOptions()
      await child(`options.${key}.item`).click()
    },
    value: async () => {
      return await child('input').inputValue()
    },
    options: async () => {
      await showOptions()
      return await child('options')
        .locator('.Select__optionLabel')
        .allInnerTexts()
    },
    delete: async (key: string) => {
      await showOptions()
      const deleteBtn = child(`options.${key}.delete`)
      await deleteBtn.click()
      await deleteBtn.waitFor({ state: 'detached' })
    }
  }
})

export const RadioButtons = createControl((self, child) => {
  const option = (key: string) => child(`options.${key}`)

  return {
    set: async (key: string) => {
      await option(key).click()
    },
    value: async () => {
      throw new Error('TODO')
    },
    isDisabled: async (key: string) => {
      return await option(key).isDisabled()
    }
  }
})
