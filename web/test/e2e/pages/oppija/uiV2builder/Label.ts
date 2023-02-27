import { createControl } from './controls'

export const Label = createControl((self) => ({
  value: () => self.innerText()
}))
