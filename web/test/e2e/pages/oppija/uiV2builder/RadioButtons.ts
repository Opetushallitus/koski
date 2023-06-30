import { createControl } from './controls'

export const RadioButtons = createControl((_self, child) => {
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
