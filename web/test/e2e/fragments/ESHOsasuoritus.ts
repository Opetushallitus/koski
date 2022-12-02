import { Locator, Page } from '@playwright/test'
import { times } from 'ramda'
import { expect } from '../base'

export class ESHOsasuoritus {
  readonly laajennaBtn: Locator
  readonly pienennäBtn: Locator
  readonly kieliInput: Locator
  readonly suorituskieliInput: Locator
  readonly laajuusInput: Locator
  readonly arvosanaInput: Locator
  readonly arviointiPäiväInput: Locator
  readonly arvioijaInput: Locator
  readonly kuvausInput: Locator

  constructor(
    private readonly page: Page,
    private readonly koulutusmoduulinTunnisteenNimi: string,
    private parent: ESHOsasuoritus | undefined = undefined
  ) {
    this.kieliInput = this.textInputLocator('osasuoritus-row', 'suoritus-cell')
    this.suorituskieliInput = this.textInputLocator(
      'osasuoritus-row',
      'suorituskieli-cell'
    )
    this.laajuusInput = this.textInputLocator('osasuoritus-row', 'laajuus-cell')

    this.laajennaBtn = this.page
      .getByTestId(`osasuoritus-row-${this.koulutusmoduulinTunnisteenNimi}`)
      .getByRole('button', {
        name: `Laajenna suoritus ${this.koulutusmoduulinTunnisteenNimi}`
      })

    this.pienennäBtn = this.page
      .getByTestId(`osasuoritus-row-${this.koulutusmoduulinTunnisteenNimi}`)
      .getByRole('button', {
        name: `Pienennä suoritus ${this.koulutusmoduulinTunnisteenNimi}`
      })

    this.arvosanaInput = this.textInputLocator(
      'osasuoritus-row',
      'arvosana-cell'
    )

    this.arviointiPäiväInput = this.textInputLocator(
      'osasuoritus-details-row',
      `päivä-value`
    )

    this.arvioijaInput = this.textInputLocator(
      'osasuoritus-details-row',
      'nimi-value'
    )

    this.kuvausInput = this.rowLocator('osasuoritus-details-row')
      .getByTestId('kuvaus-value')
      .locator('textarea')
  }

  private textInputLocator(rowPrefix: string, testId: string): Locator {
    return this.rowLocator(rowPrefix)
      .getByTestId(testId)
      .locator('input[type="text"]')
  }

  rowLocator(rowPrefix: string): Locator {
    const baseLocator =
      this.parent !== undefined
        ? this.parent.rowLocator('osasuoritukset-row')
        : this.page

    return baseLocator.getByTestId(
      `${rowPrefix}-${this.koulutusmoduulinTunnisteenNimi}`
    )
  }

  async isVisible() {
    await expect(this.rowLocator('osasuoritus-row')).toBeVisible()
  }

  async valitseKieli(kieli: string) {
    await this.kieliInput.click()

    await this.page.getByRole('listitem', { name: kieli }).click()
  }

  async valitseSuorituskieli(kieli: string) {
    await this.suorituskieliInput.click()

    await this.page.getByRole('listitem', { name: kieli }).click()
  }

  async syötäLaajuus(laajuus: string) {
    await this.laajuusInput.click()
    await this.laajuusInput.fill(laajuus)
  }

  async valitseArvosana(arvosana: string | RegExp) {
    await this.arvosanaInput.click()
    await this.page.getByRole('listitem', { name: arvosana }).click()
  }

  async syötäArviointipäivä(value: string) {
    this.arviointiPäiväInput.click()
    this.arviointiPäiväInput.fill(value)
  }

  async lisääArvioija(value: string) {
    await this.rowLocator('osasuoritus-details-row')
      .locator('a:has-text("lisää uusi")')
      .click()

    await this.arvioijaInput.click()
    await this.arvioijaInput.fill(value)
  }

  async lisääKuvaus(value: string) {
    await this.kuvausInput.click()
    await this.kuvausInput.fill(value)
  }

  async lisääOsasuoritus(koulutusmoduulinTunnisteenNimi: string) {
    await this.rowLocator('osasuoritukset-row')
      .getByRole('combobox', { name: 'Lisää alaosasuoritus' })
      .click()

    await this.rowLocator('osasuoritukset-row')
      .getByRole('listitem', { name: koulutusmoduulinTunnisteenNimi })
      .click()

    await expect(
      this.rowLocator('osasuoritukset-row').getByTestId(
        `osasuoritus-row-${koulutusmoduulinTunnisteenNimi}`
      )
    ).toBeVisible()

    const alaosasuoritus = this.getOsasuoritus(koulutusmoduulinTunnisteenNimi)
    await alaosasuoritus.isVisible()

    return alaosasuoritus
  }

  getOsasuoritus(koulutusmoduulinTunnisteenNimi: string) {
    return new ESHOsasuoritus(this.page, koulutusmoduulinTunnisteenNimi, this)
  }

  async countOsasuoritukset(className: string) {
    return await this.page.getByTestId(`tutkinnon-osa-${className}`).count()
  }
}
