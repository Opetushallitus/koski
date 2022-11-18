import { Page, Locator, expect } from '@playwright/test'
import { ESHOsasuoritus } from '../../fragments/ESHOsasuoritus'
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
  }

  async suljeKaikkiOsasuoritukset() {
    await this.suljeKaikkiOsasuorituksetBtn.click()
  }

  async poistaKaikkiOsasuoritukset() {
    const poistettavatCount = await this.page.getByRole('button', { name: 'Poista osasuoritus' }).count()

    for (let i = poistettavatCount; i >= 1; i--) {
      await this.page.getByRole('button', { name: 'Poista osasuoritus' }).nth(0).click()
      expect(this.page.getByRole('button', { name: 'Poista osasuoritus' })).toHaveCount(i - 1)
    }
  }

  async lisääOsasuoritus(
    koulutusmoduulinTunnisteenNimi: string
  ): Promise<ESHOsasuoritus> {
    await this.page.getByRole('combobox', { name: 'Lisää osasuoritus' }).click()

    await this.page
      .getByRole('listitem', {
        name: koulutusmoduulinTunnisteenNimi
      })
      .click()

    const osasuoritus = this.getOsasuoritus(
      koulutusmoduulinTunnisteenNimi
    )

    await osasuoritus.isVisible()

    return osasuoritus
  }

  getOsasuoritus(koulutusmoduulinTunnisteenNimi: string): ESHOsasuoritus {
    return new ESHOsasuoritus(this.page, koulutusmoduulinTunnisteenNimi)
  }
}
