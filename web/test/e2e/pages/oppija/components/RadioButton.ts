import { Locator, Page } from '@playwright/test'
import { expect } from '../../../base'

export class RadioButton {
  readonly page: Page
  readonly container: Locator

  static fromTestId(page: Page, testId: string) {
    return new this(page, page.getByTestId(testId))
  }

  constructor(page: Page, container: Locator) {
    this.page = page
    this.container = container
  }

  async selectOptionByLabel(label: string | RegExp) {
    // TODO: Refaktoroi selektori
    const option = this.container.locator(`[data-label="${label}"]`)
    await option.click()
    await expect(option).toHaveAttribute('data-selected', 'true')
  }
}
