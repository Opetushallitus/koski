import { Page, Locator, expect } from '@playwright/test'
import { KoskiOsasuoritus } from '../../fragments/KoskiOsasuoritus'
import { OpiskeluoikeudenTilaDialog } from './dialogs/OpiskeluoikeudentilaDialog'

export class KoskiOppijaPage {
  readonly page: Page
  readonly oppijaHeading: Locator
  readonly hetu: Locator
  readonly suoritusTabs: Locator
  readonly avaaKaikkiOsasuorituksetBtn: Locator
  readonly suljeKaikkiOsasuorituksetBtn: Locator
  readonly muokkausNäkymäBtn: Locator
  readonly tallennusBtn: Locator
  readonly peruutaMuokkausBtn: Locator
  readonly koulutusmoduuli: Locator

  readonly opiskeluoikeudenTila: OpiskeluoikeudenTilaDialog

  // TODO
  readonly opiskeluoikeudet?: Locator
  readonly osasuoritukset: Locator

  constructor(page: Page) {
    this.page = page

    this.oppijaHeading = page.getByTestId('oppija-heading')
    this.hetu = this.oppijaHeading.getByTestId('oppija-henkilotunnus')
    this.suoritusTabs = page.getByRole('tablist', { name: 'Suoritukset' })
    this.avaaKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Avaa kaikki'
    })
    this.suljeKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Sulje kaikki'
    })
    this.muokkausNäkymäBtn = page.getByRole('button', { name: 'muokkaa' })
    this.tallennusBtn = page.getByRole('button', { name: 'Tallenna' })
    this.peruutaMuokkausBtn = page.locator('a:has-text("Peruuta")') // TODO: testid
    this.koulutusmoduuli = page.getByTestId('span-for-koulutus-enum-editor')
    this.osasuoritukset = page.getByTestId('tutkinnonOsat')

    this.opiskeluoikeudenTila = new OpiskeluoikeudenTilaDialog(page)
  }

  async goto(oid: string) {
    await this.page.goto(`/koski/oppija/${oid}`)
    await expect(this.page).toHaveURL(`/koski/oppija\${oid}`)
  }

  async clickSuoritusTab(selector: number) {
    const tab = this.suoritusTabs.getByRole('tab').nth(selector)
    await tab.click()
    await expect(tab).toHaveClass(/selected/)
  }

  async clickSuoritusTabByLabel(label: string, nthOrFirst: 'first' | number) {
    const tab =
      nthOrFirst === 'first'
        ? this.suoritusTabs.getByRole('tab', { name: label }).first()
        : this.suoritusTabs.getByRole('tab', { name: label }).nth(nthOrFirst)
    await tab.click()
    await expect(tab).toHaveClass(/selected/)
  }

  async avaaKaikkiOsasuoritukset() {
    await this.avaaKaikkiOsasuorituksetBtn.click()
  }

  async suljeKaikkiOsasuoritukset() {
    await this.suljeKaikkiOsasuorituksetBtn.click()
  }

  async avaaMuokkausnäkymä() {
    // @ts-expect-error
    await this.page.evaluate(() => (window.DISABLE_EXIT_HOOKS = true))
    await this.muokkausNäkymäBtn.click()
    await this.peruutaMuokkausBtn.waitFor()
    await expect(this.page).toHaveURL(/.*\?.*\&edit=.*/)
  }

  async tallenna() {
    await this.tallennusBtn.click()
  }

  async peruuta() {
    await this.peruutaMuokkausBtn.click()
  }

  getTutkinnonOsa(testId: string) {
    return new KoskiOsasuoritus(
      this.page,
      this.osasuoritukset?.getByTestId(testId)
    )
  }
}
