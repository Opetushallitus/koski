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

type ESHOppija = BaseOppija & {
  opiskeluoikeus: 'European School of Helsinki'
  curriculum: string
  luokkaAste: string
}

type Oppija = ESHOppija

export class KoskiUusiOppijaPage {
  readonly page: Page
  readonly lisaaOppijaButton: Locator
  readonly etunimet: Locator
  readonly sukunimi: Locator
  readonly opintokokonaisuus: Dropdown
  readonly opiskeluoikeudenTila: Dropdown
  readonly opintojenRahoitus: Dropdown
  readonly submitBtn: Locator

  constructor(page: Page) {
    this.page = page
    this.lisaaOppijaButton = page.getByLabel('Tunnus')
    this.etunimet = page.getByLabel('Etunimet')
    this.sukunimi = page.getByLabel('Sukunimi')
    this.opintokokonaisuus = new Dropdown(page, 'Opintokokonaisuus')
    this.opiskeluoikeudenTila = new Dropdown(page, 'Opiskeluoikeuden tila')
    this.opintojenRahoitus = new Dropdown(page, 'Opintojen rahoitus')
    this.submitBtn = page.getByRole('button', { name: 'Lisää opiskelija' })
  }

  async goTo(hetu: Oppija['hetu']) {
    const queryParams = new URLSearchParams({
      hetu
    })
    await this.page.goto(`/koski/uusioppija#${queryParams.toString()}`)
    await expect(this.page).toHaveURL(/\/koski\/uusioppija#hetu=.+/)
  }

  async fill(oppija: Partial<BaseOppija>) {
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
    await expect(
      this.page.getByPlaceholder('henkilötunnus, nimi tai oppijanumero')
    ).toHaveText(oppija.hetu)

    return {
      submitAndExceptSuccess: async () => {}
    }
  }
}
