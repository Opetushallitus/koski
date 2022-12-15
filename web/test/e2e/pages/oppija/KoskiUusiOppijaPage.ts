import { Locator, Page, expect } from '@playwright/test'
import { Dropdown } from './components/Dropdown'

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

  async fill(oppija: Partial<BaseOppija>) {
    if (oppija.oppilaitos) {
      await this.oppilaitosTextInput.click()

      await this.oppilaitosHakuInput.fill('helsingin eurooppalainen koulu')

      await this.page
        .getByTestId(`organisaatio-list-item-${oppija.oppilaitos}`)
        .click()
    }
    if (oppija.etunimet) {
      await this.etunimet.fill(oppija.etunimet)
    }
    if (oppija.sukunimi) {
      await this.sukunimi.fill(oppija.sukunimi)
    }
    if (oppija.opintokokonaisuus) {
      await this.opintokokonaisuus.search(oppija.opintokokonaisuus)
    }
  }

  async submitAndExpectSuccess() {
    await this.submitBtn.click()
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1.2.246.562.24.\d+.*/)
  }

  async lisaaOppija(oppija: Oppija) {
    await this.goTo(oppija.hetu)
    await this.etunimet.type(oppija.etunimet)
    await expect(this.etunimet).toHaveValue(oppija.etunimet)
    await this.sukunimi.type(oppija.sukunimi)
    await expect(this.sukunimi).toHaveValue(oppija.sukunimi)
  }
}
