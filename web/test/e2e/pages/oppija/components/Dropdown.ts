import { Locator, Page } from '@playwright/test'
import { expect } from '../../../base'

export class Dropdown {
  readonly page: Page
  readonly container: Locator
  readonly textInput: Locator
  readonly options: Locator

  static fromTestId(page: Page, testId: string) {
    return new this(page, page.getByTestId(testId))
  }

  constructor(page: Page, container: Locator) {
    this.page = page
    this.container = container
    this.textInput = this.container.getByRole('combobox')
    this.options = this.container.getByRole('listitem')
  }

  async search(displayText: string) {
    await expect(this.container).toBeVisible()
    await expect(this.textInput).toBeVisible()
    await this.textInput.fill(displayText)
    await this.page.keyboard.press('Enter')
  }

  async getOptions(): Promise<Locator> {
    await this.container.click()
    return this.options
  }

  async selectOptionByClick(optionName: string | RegExp) {
    await this.textInput.click()
    await this.container
    // exact-parametri matchaa myös merkkijonot täsmällisesti.
      .getByRole('listitem', { name: optionName, exact: true })
      .click()
  }
}
