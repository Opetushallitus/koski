import { Locator, Page } from '@playwright/test'

export class KoskiOsasuoritus {
  constructor(
    private readonly page: Page,
    private readonly locator: Locator | undefined
  ) {}

  async avaa() {
    await this.page.getByTestId('oppiaine-toggle-expand').click()
  }
  async osasuoritusDropdown() {}
}
