import { Page, Locator, expect } from '@playwright/test'
import { OpiskeluoikeudenTilaDialog } from './dialogs/OpiskeluoikeudentilaDialog'

export class KoskiOppijaPage {
  readonly page: Page
  readonly oppijaHeading: Locator
  readonly hetu: Locator
  readonly koulutusmoduuli: Locator
  readonly luokka: Locator
  readonly alkamispäivä: Locator
  readonly toimipiste: Locator
  readonly suoritusTabs: Locator
  readonly muokkausNäkymäBtn: Locator
  readonly tallennusBtn: Locator
  readonly opiskeluoikeudenTila: OpiskeluoikeudenTilaDialog
  readonly tallennaBtn: Locator
  readonly peruutaMuutoksetLink: Locator
  readonly mitätöiOpiskeluoikeusLink: Locator
  readonly peruutaOpiskeluoikeudenMitätöintiLink: Locator
  readonly vahvistaOpiskeluoikeudenMitätöintiButton: Locator
  readonly opiskeluoikeudenTiedot: Locator // TODO: Refactor
  readonly opiskeluoikeudetNav: Locator
  readonly opiskeluoikeudetNavValittuVälilehti: Locator
  readonly error: Locator

  constructor(page: Page) {
    this.page = page
    // Opiskeluoikeudet nav
    this.opiskeluoikeudetNav = page.getByTestId(
      'opiskeluoikeustyypit-navigation'
    )
    this.opiskeluoikeudetNavValittuVälilehti = this.opiskeluoikeudetNav.locator(
      "[data-selected='true']"
    )
    this.oppijaHeading = page.getByTestId('oppija-heading')
    // Opiskeluoikeuden perustiedot
    this.hetu = this.oppijaHeading.getByTestId('oppija-henkilotunnus')
    this.koulutusmoduuli = this.page.getByTestId('koulutusmoduuli-value')
    this.luokka = this.page.getByTestId('luokka-value')
    this.alkamispäivä = this.page.getByTestId('alkamispäivä-value')
    this.toimipiste = this.page.getByTestId('toimipiste-value')
    // Suoritustabit
    this.suoritusTabs = page.getByRole('tablist', { name: 'Suoritukset' })
    this.tallennusBtn = page.getByRole('button', { name: 'Tallenna muutokset' })

    this.opiskeluoikeudenTila = new OpiskeluoikeudenTilaDialog(page)
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
    // TODO: Refaktoroi assertio
    this.opiskeluoikeudenTiedot = page.locator('.opiskeluoikeuden-tiedot')
    this.error = page.getByTestId('error')
  }

  async goto(oid: string) {
    await this.page.goto(`/koski/oppija/${oid}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)
  }

  async selectOpiskeluoikeus(tyyppi: string) {
    const opiskeluoikeusTab = this.opiskeluoikeudetNav.getByTestId(
      `opiskeluoikeustyyppi-${tyyppi}`
    )
    await opiskeluoikeusTab.click()
    await expect(opiskeluoikeusTab).toHaveAttribute('data-selected', 'true')
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

  async vahvistaSuoritus(
    päivämäärä: string,
    nimi: string = 'Reijo Rehtori',
    titteli: string = 'Rehtori'
  ) {
    await this.page.getByTestId('merkitse-suoritus-valmiiksi').click()

    const dialog = this.page.getByTestId('modal-dialog')

    const päivämääräInput = dialog
      .getByTestId('päivä-value')
      .locator('input[type="text"]')

    await päivämääräInput.click()

    await päivämääräInput.fill(päivämäärä)

    await dialog.getByRole('combobox', { name: 'Valitse...' }).click()

    await dialog.getByTestId('new-item').click()

    await dialog.getByRole('textbox', { name: 'nimi' }).click()

    await dialog.getByRole('textbox', { name: 'nimi' }).fill(nimi)

    await dialog.getByRole('textbox', { name: 'nimi' }).press('Tab')

    await dialog.getByPlaceholder('titteli').fill(titteli)

    await dialog.getByTestId('dialog-vahvista').click()

    await expect(
      this.page.getByTestId('merkitse-suoritus-kesken')
    ).toBeVisible()
  }

  async avaaMuokkausnäkymä() {
    await this.page.evaluate(() => {
      if ('DISABLE_EXIT_HOOKS' in window) {
        window.DISABLE_EXIT_HOOKS = true
      }
    })
    await this.muokkausNäkymäBtn.click()
    await this.peruutaMuutoksetLink.waitFor()
    await expect(this.page).toHaveURL(/.*\?.*&edit=.*/)
  }

  async peruuta() {
    await this.peruutaMuutoksetLink.click()
    // await expect(this.peruutaMuutoksetLink).toBeHidden()
    await expect(this.muokkausNäkymäBtn).toBeVisible()
  }

  async tallenna() {
    await expect(this.tallennaBtn).not.toBeDisabled()
    await this.tallennaBtn.click()
    await expect(async () => {
      const isErrorVisible = await this.error.isVisible()
      // Jos virhettä ei näy, niin edit-queryparametrin pitäisi häipyä.
      // Tällöin voidaan assertoida sitä
      if (!isErrorVisible) {
        await expect(this.page).not.toHaveURL(/&edit=/, {
          timeout: 1000
        })
      }
    }).toPass({
      timeout: 40000
    })
  }

  async peruutaMuutokset() {
    await expect(this.peruutaMuutoksetLink).toBeVisible()
    await this.peruutaMuutoksetLink.click()
  }

  async poistaViimeisinTila() {
    await this.page.getByTestId('poista-tila').click()
  }
}
