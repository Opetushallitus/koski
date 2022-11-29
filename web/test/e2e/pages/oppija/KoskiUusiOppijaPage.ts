import { Locator, Page } from '@playwright/test'
import { expect } from '../../base'

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
  // readonly kutsumanimi: Locator
  readonly sukunimi: Locator
  readonly lisaaOppijaButton: Locator

  constructor(page: Page) {
    this.page = page
    this.lisaaOppijaButton = page.getByLabel('Tunnus')
    this.etunimet = page.getByRole('textbox', { name: 'Etunimi' })
    this.sukunimi = page.getByRole('textbox', { name: 'Sukunimet' })
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
    await this.etunimet.type(oppija.etunimet)
    await expect(this.etunimet).toHaveValue(oppija.etunimet)
    await this.sukunimi.type(oppija.sukunimi)
    await expect(this.sukunimi).toHaveValue(oppija.sukunimi)
  }
}
