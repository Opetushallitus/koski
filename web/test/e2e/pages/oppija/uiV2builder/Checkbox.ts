import { createControl } from './controls'

export const Checkbox = createControl((self) => ({
  input: self,
  click: () => self.click(),
  value: () => self.isChecked(),
  isVisible: () => self.isVisible(),
  isDisabled: () => self.isDisabled(),
  waitFor: (timeout?: number) => self.waitFor({ timeout }),
  waitForToDisappear: (timeout?: number) =>
    self.waitFor({ state: 'detached', timeout })
}))
