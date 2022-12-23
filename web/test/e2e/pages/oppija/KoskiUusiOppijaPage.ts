import { Locator, Page, expect } from '@playwright/test'
import { Dropdown } from './components/Dropdown'
import { RadioButton } from './components/RadioButton'

interface BaseOppija {
  etunimet: string
  sukunimi: string
  hetu: string
  oppilaitos: string
  aloituspäivä: Date
  opiskeluoikeus: string
  suorituskieli: string
  opiskeluoikeudenTila: string
  opintokokonaisuus?: string
  oppimäärä?: string
  opintojenMaksuttomuus?: string
  peruste?: string
  rahoitus?: string
}

type ESHOppija =
  | BaseOppija
  | (BaseOppija & {
      opiskeluoikeus: 'European School of Helsinki'
      curriculum: string
      luokkaAste: string
    })

type Oppija = ESHOppija

export class KoskiUusiOppijaPage {
  readonly page: Page
  readonly etunimet: Locator
  readonly sukunimi: Locator
  readonly lisaaOppijaButton: Locator
  readonly opintokokonaisuus: Dropdown
  readonly opiskeluoikeudenTila: Dropdown
  readonly opintojenRahoitus: Dropdown
  readonly opiskeluoikeus: Dropdown
  readonly oppimäärä: Dropdown
  readonly peruste: Dropdown
  readonly maksuttomuus: RadioButton
  readonly submitBtn: Locator
  readonly oppilaitosTextInput: Locator
  readonly oppilaitosHakuInput: Locator

  constructor(page: Page) {
    this.page = page
    this.lisaaOppijaButton = page.getByLabel('Tunnus')
    this.oppilaitosTextInput = page.getByTestId('organisaatio-text-input')
    this.oppilaitosHakuInput = page.getByTestId('organisaatio-haku-input')
    this.opintokokonaisuus = Dropdown.fromTestId(
      page,
      'Opintokokonaisuus-koodisto-dropdown'
    )
    this.opiskeluoikeudenTila = Dropdown.fromTestId(
      page,
      'Opiskeluoikeuden tila-koodisto-dropdown'
    )
    this.opintojenRahoitus = Dropdown.fromTestId(
      page,
      'Opintojen rahoitus-koodisto-dropdown'
    )
    this.opiskeluoikeus = Dropdown.fromTestId(
      page,
      'Opiskeluoikeus-koodisto-dropdown'
    )
    this.maksuttomuus = RadioButton.fromTestId(
      page,
      'maksuttomuus-radio-buttons'
    )
    this.oppimäärä = Dropdown.fromTestId(page, 'Oppimäärä-koodisto-dropdown')
    this.peruste = Dropdown.fromTestId(page, 'peruste-dropdown')
    this.submitBtn = page.getByRole('button', { name: 'Lisää opiskelija' })
    this.etunimet = page.getByRole('textbox', { name: 'Etunimet' })
    this.sukunimi = page.getByRole('textbox', { name: 'Sukunimi' })
  }

  async goTo(hetu: Oppija['hetu']) {
    const queryParams = new URLSearchParams({
      hetu
    })
    await this.page.goto(`/koski/uusioppija#${queryParams.toString()}`)
    await expect(this.page).toHaveURL(/\/koski\/uusioppija#hetu=.+/)
  }

  /**
   * Täyttää oppijan tiedot Uuden opiskelijan lisäys -näkymään.
   *
   * **Huom.! Testikoodi ei tarkista, löytyykö mm. perustetta, opintokokonaisuutta tai muita opiskeluoikeus-spesifisiä kenttiä, ennen kuin niitä aletaan täyttämään.**
   * Testaa ensin luoda käyttöliittymän kautta opiskeluoikeus ja tarkista, mitkä kentät käyttöliittymässä näkyvät.
   * @param oppija Oppijan tiedot
   */
  async fill(oppija: Partial<BaseOppija>) {
    if (oppija.etunimet) {
      await this.etunimet.fill(oppija.etunimet)
    }
    if (oppija.sukunimi) {
      await this.sukunimi.fill(oppija.sukunimi)
    }
    if (oppija.oppilaitos) {
      await this.oppilaitosTextInput.click()

      await this.oppilaitosHakuInput.fill('helsingin eurooppalainen koulu')

      await this.page
        .getByTestId(`organisaatio-list-item-${oppija.oppilaitos}`)
        .click()
    }
    if (oppija.opiskeluoikeus) {
      await this.opiskeluoikeus.selectOptionByClick(oppija.opiskeluoikeus)
    }
    // TODO: Suorituskieli
    // if (oppija.suorituskieli) {
    //  ...
    // }
    if (oppija.opintokokonaisuus) {
      await this.opintokokonaisuus.search(oppija.opintokokonaisuus)
    }
    // TODO: Aloituspäivä
    // if (oppija.aloituspäivä) {
    //  ...
    // }
    if (oppija.oppimäärä) {
      await this.oppimäärä.selectOptionByClick(oppija.oppimäärä)
    }
    if (oppija.peruste) {
      await this.peruste.selectOptionByClick(oppija.peruste)
    }
    if (oppija.rahoitus) {
      await this.opintojenRahoitus.selectOptionByClick(oppija.rahoitus)
    }
    if (oppija.opintojenMaksuttomuus) {
      await this.maksuttomuus.selectOptionByLabel(oppija.opintojenMaksuttomuus)
    }
  }

  async submitAndExpectSuccess() {
    await expect(this.submitBtn).toBeEnabled()
    await this.submitBtn.click()
    await expect(this.page.getByTestId('error')).not.toBeVisible()
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1.2.246.562.24.\d+.*/)
  }
}
