import { createControl } from './controls'

export const Button = createControl((self) => ({
  button: self,
  click: () => self.click(),
  value: () => self.innerText(),
  isVisible: () => self.isVisible(),
  isDisabled: () => self.isDisabled(),
  waitFor: (timeout?: number) => self.waitFor({ timeout }),
  waitForToDisappear: (timeout?: number) =>
    self.waitFor({ state: 'detached', timeout })
}))
