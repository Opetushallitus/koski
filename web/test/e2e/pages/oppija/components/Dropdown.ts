import { Locator, Page } from '@playwright/test'

export class Dropdown {
  readonly page: Page
  readonly container: Locator
  readonly textInput: Locator
  readonly options: Locator

  constructor(page: Page, label: string) {
    this.page = page
    this.container = page.getByText(label).locator('..')
    this.textInput = this.container.getByRole('textbox')
    this.options = this.container.getByRole('listitem')
  }

  async search(displayText: string) {
    await this.textInput.fill(displayText)
    await this.page.keyboard.press('Enter')
  }

  async getOptions(): Promise<Locator> {
    await this.container.click()
    return this.options
  }
}
