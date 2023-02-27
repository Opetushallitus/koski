import { createControl } from './controls'

export const Select = createControl((self, child) => {
  const showOptions = () => child('input').click()

  return {
    set: async (key: string) => {
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
