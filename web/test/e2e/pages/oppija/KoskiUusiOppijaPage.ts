import { Locator, Page, expect } from '@playwright/test'

interface BaseOppija {
  etunimet: string
  sukunimi: string
  hetu: string
  oppilaitos: string
  aloituspäivä: Date
  opiskeluoikeus: string
  suorituskieli: string
  opiskeluoikeudenTila: string
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

  constructor(page: Page) {
    this.page = page
    this.lisaaOppijaButton = page.getByLabel('Tunnus')
  }

  async goTo(hetu: Oppija['hetu']) {
    const queryParams = new URLSearchParams({
      hetu
    })
    await this.page.goto(`/koski/uusioppija#${queryParams.toString()}`)
    await expect(this.page).toHaveURL(/\/koski\/uusioppija#hetu=.+/)
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
