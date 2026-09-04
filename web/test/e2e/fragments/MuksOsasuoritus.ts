import type { Locator, Page } from '@playwright/test'
import { Dropdown } from '../pages/oppija/components/Dropdown'

export const lisääMuksOsasuoritus = async (
  page: Page,
  nimi: string
): Promise<MuksOsasuoritus> =>
  lisääMuksOsasuoritusRyhmään(page.getByTestId('muks-osasuoritukset'), nimi)

const lisääMuksOsasuoritusRyhmään = async (
  suoritusryhmä: Locator,
  nimi: string
): Promise<MuksOsasuoritus> => {
  const page = suoritusryhmä.page()
  const dropdown = suoritusryhmä
    .locator(':scope > [data-testid="muks-osasuoritusten-lisäys"]')
    .getByTestId('dropdown-osasuoritus')

  await dropdown.click()

  const tallennettuOsasuoritus = dropdown.getByRole('listitem', {
    name: nimi,
    exact: true
  })
  if (await tallennettuOsasuoritus.isVisible()) {
    await tallennettuOsasuoritus.click()
    return muksOsasuoritus(suoritusryhmä, nimi)
  }

  await dropdown.getByTestId('new-osasuoritus').click()

  const dialog = page.getByRole('dialog').filter({
    has: page.getByRole('heading', {
      name: 'Osasuorituksen lisäys',
      exact: true
    })
  })
  await dialog
    .getByRole('textbox', {
      name: 'Opintokokonaisuuden nimi',
      exact: true
    })
    .fill(nimi)
  await dialog
    .getByRole('button', {
      name: 'Lisää osasuoritus',
      exact: true
    })
    .click()

  return muksOsasuoritus(suoritusryhmä, nimi)
}

const muksOsasuoritus = (suoritusryhmä: Locator, nimi: string) =>
  new MuksOsasuoritus(
    suoritusryhmä
      .locator(':scope > [data-testid="muks-osasuoritus"]')
      .filter({
        has: suoritusryhmä
          .page()
          .locator(':scope > [data-testid="muks-osasuoritus-yhteenveto"]')
          .getByText(nimi, { exact: true })
      })
      .last(),
    nimi
  )

export class MuksOsasuoritus {
  readonly laajuus: Locator
  readonly arvosana: Locator
  readonly arviointipäivä: Locator
  readonly arviointipäiväInput: Locator
  private readonly page: Page
  private readonly yhteenvetorivi: Locator
  private readonly tietorivi: Locator

  constructor(
    private readonly suoritusryhmä: Locator,
    private readonly nimi: string
  ) {
    this.page = suoritusryhmä.page()
    this.yhteenvetorivi = this.suoritusryhmä.locator(
      ':scope > [data-testid="muks-osasuoritus-yhteenveto"]'
    )
    this.tietorivi = this.suoritusryhmä.locator(
      ':scope > [data-testid="muks-osasuoritus-tiedot"]'
    )
    this.laajuus = this.yhteenvetorivi.getByTestId('laajuus-cell')
    this.arvosana = this.yhteenvetorivi.getByTestId('arvosana-cell')
    this.arviointipäivä = this.tietorivi.getByTestId('arviointipäivä-value')
    this.arviointipäiväInput = this.arviointipäivä.getByRole('textbox')
  }

  async avaa() {
    await this.yhteenvetorivi
      .getByRole('button', {
        name: `Laajenna suoritus ${this.nimi}`,
        exact: true
      })
      .click()
  }

  async syötäLaajuus(arvo: number | '') {
    await this.yhteenvetorivi
      .getByTestId('laajuus-editor')
      .getByTestId('number-editor')
      .fill(arvo.toString())
  }

  async valitseArvosana(arvo: string) {
    const arvosanaDropdown = new Dropdown(
      this.page,
      this.yhteenvetorivi.getByTestId('enum-editor-dropdown')
    )
    await arvosanaDropdown.selectOptionByClick(arvo)
  }

  async syötäArviointipäivä(arvo: string) {
    await this.arviointipäiväInput.fill(arvo)
  }

  alaosasuoritus(nimi: string) {
    return muksOsasuoritus(
      this.suoritusryhmä.getByTestId('muks-alaosasuoritukset'),
      nimi
    )
  }

  async lisääAlaosasuoritus(nimi: string): Promise<MuksOsasuoritus> {
    return lisääMuksOsasuoritusRyhmään(
      this.suoritusryhmä.getByTestId('muks-alaosasuoritukset'),
      nimi
    )
  }
}
