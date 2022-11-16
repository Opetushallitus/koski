import { Page, Locator, expect } from '@playwright/test'
import { KoskiOsasuoritus } from '../../fragments/KoskiOsasuoritus'

export class KoskiOppijaPage {
  readonly page: Page
  readonly oppijaHeading: Locator
  readonly hetu: Locator
  readonly suoritusTabs: Locator
  readonly avaaKaikkiOsasuorituksetBtn: Locator
  readonly suljeKaikkiOsasuorituksetBtn: Locator
  readonly muokkausNäkymäBtn: Locator

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
    this.osasuoritukset = page.getByTestId('tutkinnonOsat')
  }

  async goto(oid: string) {
    await this.page.goto(`/koski/oppija/${oid}`)
    await expect(this.page).toHaveURL(`/koski/oppija\${oid}`)
  }

  async clickSuoritusTab(selector: number) {
    const tab = this.suoritusTabs.getByRole('tab').nth(selector)
    await tab.click()
    expect(await tab.getAttribute('class')).toContain('selected')
  }

  async avaaKaikkiOsasuoritukset() {
    await this.avaaKaikkiOsasuorituksetBtn.click()
  }

  async suljeKaikkiOsasuoritukset() {
    await this.suljeKaikkiOsasuorituksetBtn.click()
  }

  async avaaMuokkausnäkymä() {
    await this.muokkausNäkymäBtn.click()
  }

  getTutkinnonOsa(testId: string) {
    return new KoskiOsasuoritus(
      this.page,
      this.osasuoritukset?.getByTestId(testId)
    )
  }
}
