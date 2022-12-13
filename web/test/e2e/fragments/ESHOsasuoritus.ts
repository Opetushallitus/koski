import { Locator, Page } from '@playwright/test'
import { expect } from '../base'
import { Dropdown } from '../pages/oppija/components/Dropdown'

export class ESHOsasuoritus {
  // Validaatiot on välillä tosi hitaita käyttöliittymässä.
  readonly validaatioTimeout = 5000

  readonly laajennaBtn: Locator
  readonly pienennäBtn: Locator
  readonly kieliDropdown: Dropdown
  readonly suorituskieliDropdown: Dropdown
  readonly laajuusInput: Locator
  readonly arvosanaDropdown: Dropdown
  readonly arviointiPäiväInput: Locator
  readonly arvioijaInput: Locator
  readonly kuvausInput: Locator

  constructor(
    private readonly page: Page,
    private readonly koulutusmoduulinTunnisteenNimi: string,
    private parent: ESHOsasuoritus | undefined = undefined
  ) {
    this.kieliDropdown = new Dropdown(
      this.page,
      this.testIdLocator('osasuoritus-row', 'suoritus-cell').getByTestId(
        'enum-editor-dropdown'
      )
    )
    this.suorituskieliDropdown = new Dropdown(
      this.page,
      this.testIdLocator('osasuoritus-row', 'suorituskieli-cell').getByTestId(
        'enum-editor-dropdown'
      )
    )
    this.laajuusInput = this.textInputLocator('osasuoritus-row', 'laajuus-cell')

    this.laajennaBtn = this.rowLocator('osasuoritus-row').getByRole('button', {
      name: `Laajenna suoritus ${this.koulutusmoduulinTunnisteenNimi}`
    })

    this.pienennäBtn = this.rowLocator('osasuoritus-row').getByRole('button', {
      name: `Pienennä suoritus ${this.koulutusmoduulinTunnisteenNimi}`
    })

    this.arvosanaDropdown = new Dropdown(
      this.page,
      this.testIdLocator('osasuoritus-row', 'arvosana-cell').getByTestId(
        'enum-editor-dropdown'
      )
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
    return this.testIdLocator(rowPrefix, testId).locator('input[type="text"]')
  }

  private testIdLocator(rowPrefix: string, testId: string): Locator {
    return this.rowLocator(rowPrefix).getByTestId(testId)
  }

  rowLocator(rowPrefix: string): Locator {
    return this.baseLocator().getByTestId(
      `${rowPrefix}-${this.koulutusmoduulinTunnisteenNimi}`
    )
  }

  private baseLocator(): Locator | Page {
    return this.parent !== undefined
      ? this.parent.rowLocator('osasuoritukset-row')
      : this.page
  }

  async isVisible() {
    await expect(this.rowLocator('osasuoritus-row')).toBeVisible()
  }

  async laajenna() {
    await this.laajennaBtn.click()
    await expect(this.pienennäBtn).toBeVisible()
  }

  async pienennä() {
    await this.pienennäBtn.click()
    await expect(this.laajennaBtn).toBeVisible()
  }

  async valitseKieli(
    kieli: string,
    poistuvaValidaatiovirhe:
      | string
      | undefined = 'Kielioppiaineella on oltava valittuna kieli.'
  ) {
    const validaatioElement = this.validaatioElementLocator(
      poistuvaValidaatiovirhe
    )

    if (poistuvaValidaatiovirhe !== undefined) {
      await expect(validaatioElement).toBeVisible()
    }

    await this.kieliDropdown.selectOptionByClick(kieli)

    if (poistuvaValidaatiovirhe !== undefined) {
      await expect(validaatioElement).not.toBeVisible({
        timeout: this.validaatioTimeout
      })
    }
  }

  async valitseSuorituskieli(
    kieli: string,
    poistuvaValidaatiovirhe:
      | string
      | undefined = 'Suorituksella on oltava valittuna suorituskieli.'
  ) {
    const validaatioElement = this.validaatioElementLocator(
      poistuvaValidaatiovirhe
    )

    if (poistuvaValidaatiovirhe !== undefined) {
      await expect(validaatioElement).toBeVisible()
    }

    await this.suorituskieliDropdown.selectOptionByClick(kieli)

    if (poistuvaValidaatiovirhe !== undefined) {
      await expect(validaatioElement).not.toBeVisible({
        timeout: this.validaatioTimeout
      })
    }
  }

  private validaatioElementLocator(poistuvaValidaatiovirhe: string) {
    return this.baseLocator().locator(
      `[data-testid="osasuoritus-row-${this.koulutusmoduulinTunnisteenNimi}"] ~ tr.error [aria-label="${poistuvaValidaatiovirhe}"]`
    )
  }

  async syötäLaajuus(laajuus: string, validaatioVirhePoistuu: boolean = true) {
    if (validaatioVirhePoistuu) {
      await expect(this.laajuusInput).toHaveClass(/error/)
    }
    await this.laajuusInput.click()
    await this.laajuusInput.fill(laajuus)
    await this.laajuusInput.evaluate((e) => e.blur())

    if (validaatioVirhePoistuu) {
      await expect(this.laajuusInput).not.toHaveClass(/error/, {
        timeout: this.validaatioTimeout
      })
    }
  }

  async valitseArvosana(arvosana: string | RegExp) {
    await this.arvosanaDropdown.selectOptionByClick(arvosana)
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

  async poista() {
    await this.rowLocator('osasuoritus-row')
      .getByRole('button', { name: 'Poista osasuoritus' })
      .click()

    await expect(this.rowLocator('osasuoritus-row')).not.toBeVisible()
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

    // Kaikenlaista tuli kokeiltua, mutta jokin bugaa ja jos esim. tämän jälkeen
    // testissä klikkaa dropdown elementtiä, voi sen avaama lista sulkeutua
    await this.page.waitForTimeout(1000)

    return alaosasuoritus
  }

  getOsasuoritus(koulutusmoduulinTunnisteenNimi: string) {
    return new ESHOsasuoritus(this.page, koulutusmoduulinTunnisteenNimi, this)
  }

  async countOsasuoritukset(className: string) {
    return await this.page
      .getByTestId(`tutkinnon-osa`)
      .locator(`[data-test-osasuoritus-class="${className}"]`)
      .count()
  }
}
