import { createControl } from './controls'

export const Select = createControl((self, child) => {
  const showOptions = async () => {
    await child('input').click()
  }

  return {
    input: child('input'),
    set: async (key: string) => {
      await showOptions()
      try {
        await child(`options.${key}.item`).click({ timeout: 5000 })
      } catch (e) {
        const x = await child('options')
          .locator('.Select__optionLabel')
          .elementHandles()
        console.error(
          'Select-laatikon itemin valinta ei onnistunut, mutta tällaisia olisi tarjolla:',
          await Promise.all(
            x.map(async (a) => await a.getAttribute('data-testid'))
          )
        )
        throw e
      }
      await child('options').waitFor({ state: 'hidden' })
      // await new Promise((resolve) => setTimeout(resolve, 1000)) // Todella rumaa flakyn korjailua
    },
    setByLabel: async (text: string) => {
      await showOptions()
      await child('options').getByText(text).click()
    },
    type: async (text: string) => {
      await child('input').fill(text)
    },
    selectFirstOption: async () => {
      await showOptions()
      await child('options').first().click()
    },
    value: async () => {
      return await child('input').inputValue()
    },
    options: async () => {
      await showOptions()
      const options = await child('options')
        .locator('.Select__optionLabel')
        .allInnerTexts()
      return options.filter((o) => o !== 'Ei valintaa')
    },
    delete: async (key: string) => {
      await showOptions()
      const deleteBtn = child(`options.${key}.delete`)
      await deleteBtn.click()
      await deleteBtn.waitFor({ state: 'detached' })
    }
  }
})
