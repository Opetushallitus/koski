import { createControl } from './controls'

export const RadioButtons = <T extends string>(...optionIds: T[]) =>
  createControl((_self, child) => {
    const option = (key: T) => child(`options.${key}`)

    return {
      set: async (key: T) => {
        await option(key).click({ force: true })
      },
      value: async () => {
        for (const id of optionIds) {
          if (await option(id).isChecked()) {
            return id
          }
        }
        return ''
      },
      isDisabled: async (key: T) => {
        return await option(key).isDisabled()
      }
    }
  })
