import { Locator, Page, expect } from '@playwright/test'

export class Dropdown {
  readonly page: Page
  readonly container: Locator
  readonly textInput: Locator
  readonly options: Locator

  constructor(page: Page, testId: string) {
    this.page = page
    this.container = this.page.getByTestId(testId)
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
}
