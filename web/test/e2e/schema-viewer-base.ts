import { expect, test as base } from './base'

export const test = base.extend<{ checkPageErrors: void }>({
  checkPageErrors: [
    async ({ page }, use) => {
      const errors: Error[] = []
      const onPageError = (error: Error) => errors.push(error)
      page.on('pageerror', onPageError)
      await use()
      page.off('pageerror', onPageError)
      expect(errors).toEqual([])
    },
    { auto: true }
  ]
})

export { expect } from './base'
