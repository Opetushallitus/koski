import { createControl } from './controls'

export const DateInput = createControl((self, child) => ({
  click: () => self.click(),
  value: () => child('edit.input').inputValue(),
  set: async (value: string) => {
    const input = child('edit.input')
    await input.clear()
    await input.fill(value)
  }
}))
