import type { Locator, Page } from '@playwright/test'
import {
  lisääMuksOsasuoritus,
  MuksOsasuoritus
} from '../../fragments/MuksOsasuoritus'
import { KoskiOppijaPage } from './KoskiOppijaPage'

export class KoskiMuksOppijaPage extends KoskiOppijaPage {
  readonly opintokokonaisuus: Locator
  readonly merkitseSuoritusValmiiksiBtn: Locator
  readonly merkitseValmiiksiDialogVahvistaBtn: Locator

  constructor(page: Page) {
    super(page)

    this.opintokokonaisuus = page.getByTestId(
      'hyperlink-for-opintokokonaisuudet-enum-editor'
    )
    this.merkitseSuoritusValmiiksiBtn = page.getByTestId(
      'merkitse-suoritus-valmiiksi'
    )
    this.merkitseValmiiksiDialogVahvistaBtn =
      page.getByTestId('dialog-vahvista')
  }

  async lisääOsasuoritus(nimi: string): Promise<MuksOsasuoritus> {
    return lisääMuksOsasuoritus(this.page, nimi)
  }

  async merkitseSuoritusValmiiksi() {
    await this.merkitseSuoritusValmiiksiBtn.click()
    await this.merkitseValmiiksiDialogVahvistaBtn.click()
  }
}
