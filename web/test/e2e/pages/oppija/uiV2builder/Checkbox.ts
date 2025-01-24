import { createControl } from './controls'

export const Checkbox = createControl((self, child) => {
  const input = child('input')
  const label = child('label')
  return {
    input,
    click: () => label.click(),
    value: () => input.isChecked(),
    isVisible: () => self.isVisible(),
    isDisabled: () => self.isDisabled(),
    waitFor: (timeout?: number) => self.waitFor({ timeout }),
    waitForToDisappear: (timeout?: number) =>
      self.waitFor({ state: 'detached', timeout })
  }
})
