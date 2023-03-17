import { createControl } from './controls'

export const Label = createControl((self) => ({
  value: () => self.innerText(),
  waitFor: (timeout?: number) => self.waitFor({ timeout }),
  waitForToDisappear: (timeout?: number) =>
    self.waitFor({ state: 'detached', timeout })
}))
