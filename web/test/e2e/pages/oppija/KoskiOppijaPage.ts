import { Page, Locator, expect } from '@playwright/test'
import { KoskiOsasuoritus } from '../../fragments/KoskiOsasuoritus'

export class KoskiOppijaPage {
  readonly page: Page
  readonly oppijaHeading: Locator
  readonly hetu: Locator
  readonly koulutusmoduuli: Locator
  readonly luokka: Locator
  readonly alkamispäivä: Locator
  readonly toimipiste: Locator
  readonly suoritusTabs: Locator
  readonly avaaKaikkiOsasuorituksetBtn: Locator
  readonly suljeKaikkiOsasuorituksetBtn: Locator
  readonly muokkausNäkymäBtn: Locator
  readonly tallennaBtn: Locator
  readonly peruutaMuutoksetLink: Locator
  readonly mitätöiOpiskeluoikeusLink: Locator
  readonly peruutaOpiskeluoikeudenMitätöintiLink: Locator
  readonly vahvistaOpiskeluoikeudenMitätöintiButton: Locator

  readonly opiskeluoikeudet?: Locator
  readonly osasuoritukset: Locator

  constructor(page: Page) {
    this.page = page
    // Oppija heading
    this.oppijaHeading = page.getByTestId('oppija-heading')
    // Opiskeluoikeuden perustiedot
    this.hetu = this.oppijaHeading.getByTestId('oppija-henkilotunnus')
    this.koulutusmoduuli = this.page.getByTestId('koulutusmoduuli-value')
    this.luokka = this.page.getByTestId('luokka-value')
    this.alkamispäivä = this.page.getByTestId('alkamispäivä-value')
    this.toimipiste = this.page.getByTestId('toimipiste-value')
    // Suoritustabit
    this.suoritusTabs = page.getByRole('tablist', { name: 'Suoritukset' })
    // Napit jne..
    this.avaaKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Avaa kaikki'
    })
    this.suljeKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Sulje kaikki'
    })
    this.muokkausNäkymäBtn = page.getByRole('button', {
      name: 'Muokkaa opiskeluoikeutta'
    })
    this.peruutaMuutoksetLink = page.getByRole('link', {
      name: 'Peruuta muutokset'
    })
    this.tallennaBtn = page.getByRole('button', {
      name: 'Tallenna muutokset'
    })
    this.mitätöiOpiskeluoikeusLink = page.getByRole('link', {
      name: 'Mitätöi opiskeluoikeus'
    })
    this.peruutaOpiskeluoikeudenMitätöintiLink = page.getByRole('link', {
      name: 'Peruuta mitätöinti'
    })
    this.vahvistaOpiskeluoikeudenMitätöintiButton = page.getByRole('link', {
      name: 'Vahvista mitätöinti, operaatiota ei voi peruuttaa'
    })

    // TODO: Wrappaa opaan Page Object Modeliin
    this.osasuoritukset = page.getByTestId('tutkinnonOsat')
  }

  async goto(oid: string) {
    await this.page.goto(`/koski/oppija/${oid}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)
  }

  async clickNthSuoritusTab(selector: number) {
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
    await this.muokkausNäkymäBtn.click()
    await expect(this.muokkausNäkymäBtn).not.toBeVisible()
  }

  async tallenna() {
    await expect(this.tallennaBtn).not.toBeDisabled()
    await this.tallennaBtn.click()
  }

  async peruutaMuutokset() {
    await expect(this.peruutaMuutoksetLink).toBeVisible()
    await this.peruutaMuutoksetLink.click()
  }

  getTutkinnonOsa(testId: string) {
    return new KoskiOsasuoritus(
      this.page,
      this.osasuoritukset?.getByTestId(testId)
    )
  }
}
