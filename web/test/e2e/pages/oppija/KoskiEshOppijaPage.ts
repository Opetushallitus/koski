import { Page, Locator, expect } from '@playwright/test'
import { ESHOsasuoritus } from '../../fragments/ESHOsasuoritus'
import { Dropdown } from './components/Dropdown'
import { KoskiOppijaPage } from './KoskiOppijaPage'

export class KoskiEshOppijaPage extends KoskiOppijaPage {
  readonly avaaKaikkiOsasuorituksetBtn: Locator
  readonly suljeKaikkiOsasuorituksetBtn: Locator
  readonly opiskeluoikeudet?: Locator

  constructor(page: Page) {
    super(page)

    this.avaaKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Avaa kaikki'
    })
    this.suljeKaikkiOsasuorituksetBtn = page.getByRole('button', {
      name: 'Sulje kaikki'
    })
  }

  async avaaKaikkiOsasuoritukset() {
    await this.avaaKaikkiOsasuorituksetBtn.click()
    await expect(this.suljeKaikkiOsasuorituksetBtn).toBeVisible()
    await expect(
      this.page
        .getByTestId('oppiaineet-list')
        .getByTestId('suoritus-taulukko')
        .locator(`[data-test-nested-level="0"][data-test-expanded="false"]`)
    ).toHaveCount(0, { timeout: 20000 })
  }

  async suljeKaikkiOsasuoritukset() {
    await this.suljeKaikkiOsasuorituksetBtn.click()
    await expect(this.avaaKaikkiOsasuorituksetBtn).toBeVisible()
    await expect(
      this.page
        .getByTestId('oppiaineet-list')
        .getByTestId('suoritus-taulukko')
        .locator(`[data-test-nested-level="0"][data-test-expanded="true"]`)
    ).toHaveCount(0, { timeout: 20000 })
  }

  async poistaKaikkiOsasuoritukset() {
    const poistettavatCount = await this.page
      .getByRole('button', { name: 'Poista osasuoritus' })
      .count()

    for (let i = poistettavatCount; i >= 1; i--) {
      await this.page
        .getByRole('button', { name: 'Poista osasuoritus' })
        .nth(0)
        .click()
      await expect(
        this.page.getByRole('button', { name: 'Poista osasuoritus' })
      ).toHaveCount(i - 1)
    }
  }

  async lisääOsasuoritus(
    koulutusmoduulinTunnisteenNimi: string
  ): Promise<ESHOsasuoritus> {
    const dropdown = Dropdown.fromTestId(this.page, 'uusi-osasuoritus')
    await dropdown.selectOptionByClick(koulutusmoduulinTunnisteenNimi)

    const osasuoritus = this.getOsasuoritus(koulutusmoduulinTunnisteenNimi)

    await osasuoritus.isVisible()

    return osasuoritus
  }

  async expectSuoritusUrl(suoritus: string, hyväksyttyPostfix: string = '.*') {
    await expect(this.page).toHaveURL(
      new RegExp(
        `koski\\/oppija\\/1\\.2\\..*\\?1\\.2\\..*\\.suoritus=${suoritus}${hyväksyttyPostfix}$`
      ),
      { timeout: 30000 }
    )
  }

  async poistaSuoritus(
    vuosiluokka: string,
    aktiivinenSuoritusPoistonJälkeen: string = 'EB-tutkinto'
  ) {
    await this.clickSuoritusTabByLabel(vuosiluokka, 'first')
    await this.avaaMuokkausnäkymä()
    await this.page.getByRole('link', { name: 'Poista suoritus' }).click()
    await this.page
      .getByRole('button', {
        name: 'Vahvista poisto, operaatiota ei voi peruuttaa'
      })
      .click()
    await this.expectSuoritusUrl(aktiivinenSuoritusPoistonJälkeen)
  }

  async lisääVuosiluokanSuoritus(vuosiluokka: string, alkamispäivä: string) {
    await this.page.getByRole('button', { name: 'lisää suoritus' }).click()
    await this.page.getByTestId('Suoritus-koodisto-dropdown').click()
    await this.page.getByRole('listitem', { name: vuosiluokka }).click()

    const aloituspaivaInput = this.page
      .getByTestId('aloituspaiva-input')
      .locator('input[type="text"]')
    await aloituspaivaInput.click()
    await aloituspaivaInput.fill(alkamispäivä)

    await this.page.getByTestId('dialog-vahvista').click()
    await this.expectSuoritusUrl(vuosiluokka)
  }

  async lisääEBTutkinnonSuoritus() {
    await this.page.getByRole('button', { name: 'lisää suoritus' }).click()

    const suoritusDropdown = Dropdown.fromTestId(
      this.page,
      'Suoritus-koodisto-dropdown'
    )
    await suoritusDropdown.selectOptionByClick(
      'EB-tutkinto (European Baccalaureate)'
    )

    await this.page.getByTestId('dialog-vahvista').click()
    await this.expectSuoritusUrl('EB-tutkinto')
  }

  getOsasuoritus(koulutusmoduulinTunnisteenNimi: string): ESHOsasuoritus {
    return new ESHOsasuoritus(this.page, koulutusmoduulinTunnisteenNimi)
  }
}
