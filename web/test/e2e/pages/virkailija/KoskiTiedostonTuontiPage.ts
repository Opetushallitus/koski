import { Locator, Page } from '@playwright/test'

export class KoskiTiedostonTuontiPage {
  readonly naviLinkki: Locator
  readonly otsikko: Locator
  readonly esimerkki: Locator
  readonly esimerkkirivi: Locator
  readonly tiedostokenttä: Locator
  readonly esikatseleButton: Locator
  readonly tuoButton: Locator
  readonly tyhjennäButton: Locator
  readonly tulos: Locator
  readonly tuloksenOtsikko: Locator
  readonly tulosilmoitus: Locator
  readonly uudet: Locator
  readonly päivitykset: Locator
  readonly hylätyt: Locator
  readonly jäsentämättömät: Locator
  readonly rivit: Locator
  readonly jäsennysvirheet: Locator

  constructor(private readonly page: Page) {
    this.naviLinkki = page.locator('.tiedonsiirrot-navi .tiedoston-tuonti-link')
    this.otsikko = page.locator('.tiedoston-tuonti > h2')
    this.esimerkki = page.getByTestId('tiedoston-tuonti.esimerkki')
    this.esimerkkirivi = this.esimerkki.locator('pre')
    this.tiedostokenttä = page.getByTestId('tiedoston-tuonti.tiedosto')
    this.esikatseleButton = page.getByTestId('tiedoston-tuonti.esikatsele')
    this.tuoButton = page.getByTestId('tiedoston-tuonti.tuo')
    this.tyhjennäButton = page.getByTestId('tiedoston-tuonti.tyhjennä')
    this.tulos = page.getByTestId('tiedoston-tuonti.tulos')
    this.tuloksenOtsikko = this.tulos.locator('h3')
    this.tulosilmoitus = page.getByTestId('tiedoston-tuonti.tulosilmoitus')
    this.uudet = page.getByTestId('tiedoston-tuonti.uudet')
    this.päivitykset = page.getByTestId('tiedoston-tuonti.päivitykset')
    this.hylätyt = page.getByTestId('tiedoston-tuonti.hylätyt')
    this.jäsentämättömät = page.getByTestId('tiedoston-tuonti.jäsentämättömät')
    this.rivit = page.getByTestId('tiedoston-tuonti.rivit').locator('tbody tr')
    this.jäsennysvirheet = page
      .getByTestId('tiedoston-tuonti.jäsennysvirheet')
      .locator('tbody tr')
  }

  tila(rivi: number): Locator {
    return this.rivit.nth(rivi).locator('td').nth(1)
  }

  async goto() {
    await this.page.goto('/koski/tiedonsiirrot/tiedoston-tuonti')
  }

  async valitseTiedosto(sisältö: string) {
    await this.tiedostokenttä.setInputFiles({
      name: 'ibis.csv',
      mimeType: 'text/csv',
      buffer: Buffer.from(sisältö, 'utf-8')
    })
  }
}
