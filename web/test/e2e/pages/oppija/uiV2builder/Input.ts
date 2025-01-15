import { createControl } from './controls'

export const Input = createControl((self, child) => ({
  elem: child('input'),
  click: () => self.click(),
  value: () => child('input').inputValue(),
  set: async (value: string) => {
    const input = child('input')
    await input.clear()
    await input.fill(value)
  }
}))
