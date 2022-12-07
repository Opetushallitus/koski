import { Locator, Page } from '@playwright/test'
import { OsasuoritusDialog } from './dialogs/OsasuoritusDialog'
import { KoskiOppijaPage } from './KoskiOppijaPage'

export class KoskiMuksOppijaPage extends KoskiOppijaPage {
  readonly opintokokonaisuus: Locator
  readonly osasuoritusDialog: OsasuoritusDialog
  readonly merkitseSuoritusValmiiksiBtn: Locator
  readonly merkitseValmiiksiDialogVahvistaBtn: Locator

  constructor(page: Page) {
    super(page)

    this.osasuoritusDialog = new OsasuoritusDialog(page)
    this.opintokokonaisuus = page.getByTestId(
      'hyperlink-for-opintokokonaisuudet-enum-editor'
    )
    this.merkitseSuoritusValmiiksiBtn = page.getByTestId(
      'merkitse-suoritus-valmiiksi'
    )
    this.merkitseValmiiksiDialogVahvistaBtn =
      page.getByTestId('dialog-vahvista')
  }

  async lisääUusiOsasuoritus(dropdownIndex: number, nimi: string) {
    await this.osasuoritusDialog.lisääUusiOsasuoritus(nimi, (x) =>
      x.nth(dropdownIndex)
    )
  }

  async lisääTallennettuOsasuoritus(dropdownIndex: number, nimi: string) {
    await this.osasuoritusDialog.lisääTallennettuOsasuoritus(nimi, (x) =>
      x.nth(dropdownIndex)
    )
  }

  async setOsasuorituksenLaajuus(laajuusEditorIndex: number, arvo: number) {
    await this.getLaajuusEditor(laajuusEditorIndex).fill(arvo.toString())
  }

  getLaajuusEditor(nthLaajuusEditor: number) {
    return this.page
      .locator('.osasuoritukset') // TODO: testid
      .getByTestId('laajuus-editor')
      .nth(nthLaajuusEditor)
      .getByTestId('number-editor')
  }

  async merkitseSuoritusValmiiksi() {
    await this.merkitseSuoritusValmiiksiBtn.click()
    await this.merkitseValmiiksiDialogVahvistaBtn.click()
  }
}
