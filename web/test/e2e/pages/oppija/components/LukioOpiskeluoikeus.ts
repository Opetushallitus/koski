import { Locator } from '@playwright/test'
import { expect } from '../../../base'
import { Dropdown } from './Dropdown'

export class LukioOpiskeluoikeus {
  readonly container: Locator
  readonly tutkinnonNimi: Locator
  readonly opiskeluoikeudenTiedot: Locator
  readonly omanÄidinkielenOpinnot: Locator
  readonly tilaVahvistus: Locator
  readonly oppiaineet: Locator
  readonly päättymispäivä: Locator

  constructor(container: Locator) {
    this.container = container
    this.tutkinnonNimi = this.kenttä('koulutusmoduuli').locator('.tunniste')
    this.opiskeluoikeudenTiedot = this.container.locator(
      '.opiskeluoikeuden-tiedot'
    )
    this.omanÄidinkielenOpinnot = this.kenttä('omanÄidinkielenOpinnot')
    this.tilaVahvistus = this.container.locator('.suoritus > .tila-vahvistus')
    this.oppiaineet = this.container.locator('table.suoritukset.oppiaineet')
    this.päättymispäivä = this.container.locator('.päättymispäivä')
  }

  oppiaineRivi(koodi: string): Locator {
    return this.oppiaineet.locator(`.oppiaine-rivi.${koodi}`)
  }

  async odotaTilaHistoria(
    odotetut: { päivä: string; tila: string }[]
  ): Promise<void> {
    const jaksot = this.kenttä('tila').locator('.opiskeluoikeusjakso')
    for (let i = 0; i < odotetut.length; i++) {
      await expect(jaksot.nth(i).locator('label.date')).toHaveText(
        odotetut[i].päivä
      )
      await expect(jaksot.nth(i).locator('label.tila')).toHaveText(
        odotetut[i].tila
      )
    }
  }

  async lisääTila(tila: string): Promise<void> {
    await this.container
      .getByTestId('lisää-opiskeluoikeuden-tila-btn')
      .click()
    const dialog = this.container.page().getByRole('dialog')
    await dialog.getByRole('radio', { name: tila }).click()
    await dialog.getByTestId('dialog-vahvista').click()
  }

  kurssi(oppiaineRivi: Locator, koodi: string): Locator {
    return oppiaineRivi.locator('li.kurssi').filter({
      has: oppiaineRivi.page().getByRole('button', { name: koodi, exact: true })
    })
  }

  kieliaineenKieliDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      oppiaineRivi
        .locator('.title .properties .dropdown-wrapper')
        .getByTestId('enum-editor-dropdown')
    )
  }

  suorituskieliDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      this.kenttä('suorituskieli', oppiaineRivi).getByTestId(
        'enum-editor-dropdown'
      )
    )
  }

  erityinenTutkintoCheckbox(juuri: Locator = this.container): Locator {
    return this.kenttä('suoritettuErityisenäTutkintona', juuri).getByRole(
      'checkbox'
    )
  }

  pakollinenCheckbox(juuri: Locator): Locator {
    return this.kenttä('pakollinen', juuri).getByRole('checkbox')
  }

  laajuusInput(oppiaineRivi: Locator): Locator {
    return oppiaineRivi
      .locator('.laajuus:not(.arvioitu)')
      .getByTestId('number-editor')
  }

  arvosanaDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      oppiaineRivi.locator('td.arvosana').getByTestId('enum-editor-dropdown')
    )
  }

  private async uusiOppiaineDropdown(): Promise<Dropdown> {
    const uusiOppiaine = this.container.locator('.uusi-oppiaine').first()
    await expect(
      uusiOppiaine.locator('.ajax-loading-placeholder')
    ).toHaveCount(0)
    return Dropdown.fromComboboxName(uusiOppiaine, 'Lisää oppiaine')
  }

  async lisääOppiaine(nimi: string): Promise<void> {
    const dropdown = await this.uusiOppiaineDropdown()
    await dropdown.selectOptionByClick(nimi)
  }

  // Poistolinkillä ei ole näkyvää tekstiä, aria-labelia eikä title-attribuuttia
  // - CSS on ainoa toimiva valinta, ei roolin/nimen puutteen vuoksi oikotie.
  async poistaOppiaine(oppiaineRivi: Locator): Promise<void> {
    await oppiaineRivi.locator('.remove-row a').click()
  }

  async lisääPaikallinenOppiaine(): Promise<Locator> {
    const dropdown = await this.uusiOppiaineDropdown()
    await dropdown.selectOptionByClick('Lisää...')
    return this.oppiaineet.locator('.oppiaine-rivi').last()
  }

  private paikallisenOppiaineenKoodi(oppiaineRivi: Locator): Locator {
    return oppiaineRivi.getByPlaceholder('Koodi')
  }

  private paikallisenOppiaineenNimi(oppiaineRivi: Locator): Locator {
    return oppiaineRivi.getByPlaceholder('Oppiaineen nimi')
  }

  async lisääJaTäytäPaikallinenOppiaine(tiedot: {
    koodi: string
    nimi: string
    kuvaus: string
    arvosana: string
  }): Promise<Locator> {
    const rivi = await this.lisääPaikallinenOppiaine()
    await this.paikallisenOppiaineenKoodi(rivi).fill(tiedot.koodi)
    await this.paikallisenOppiaineenNimi(rivi).fill(tiedot.nimi)
    await this.kenttä('kuvaus', rivi).locator('textarea').fill(tiedot.kuvaus)
    await this.arvosanaDropdown(rivi).selectOptionByClick(tiedot.arvosana)
    return rivi
  }

  private async avaaLisääOsasuoritusDialogi(
    oppiaineRivi: Locator
  ): Promise<{ dialog: Locator; kurssiDropdown: Dropdown }> {
    await oppiaineRivi.getByText('Lisää osasuoritus').click()
    const page = oppiaineRivi.page()
    const dialog = page.getByRole('dialog')
    await expect(dialog.locator('.ajax-loading-placeholder')).toHaveCount(0)
    const kurssiDropdown = new Dropdown(page, dialog.locator('.dropdown'))
    return { dialog, kurssiDropdown }
  }

  private async vahvistaLisääOsasuoritusDialogi(
    oppiaineRivi: Locator,
    dialog: Locator,
    koodi: string
  ): Promise<Locator> {
    await expect(dialog.getByTestId('dialog-vahvista')).toBeEnabled()
    await dialog.getByTestId('dialog-vahvista').click()
    return this.kurssi(oppiaineRivi, koodi)
  }

  async lisääPaikallinenKurssi(
    oppiaineRivi: Locator,
    tiedot: { tunniste: string; nimi: string; kuvaus: string; laajuus: string }
  ): Promise<Locator> {
    const { dialog, kurssiDropdown } =
      await this.avaaLisääOsasuoritusDialogi(oppiaineRivi)
    await kurssiDropdown.selectOptionByClick('Lisää paikallinen kurssi...')

    await dialog
      .getByTestId('koodiarvo-value')
      .locator('input')
      .fill(tiedot.tunniste)
    await dialog.getByTestId('nimi-value').locator('input').fill(tiedot.nimi)
    await dialog
      .getByTestId('laajuus-value')
      .getByTestId('number-editor')
      .fill(tiedot.laajuus)
    await dialog
      .getByTestId('kuvaus-value')
      .locator('textarea')
      .fill(tiedot.kuvaus)

    return this.vahvistaLisääOsasuoritusDialogi(
      oppiaineRivi,
      dialog,
      tiedot.tunniste
    )
  }

  kurssinArvosanaDropdown(kurssi: Locator): Dropdown {
    return new Dropdown(kurssi.page(), kurssi.getByTestId('enum-editor-dropdown'))
  }

  async lisääValtakunnallinenKurssi(
    oppiaineRivi: Locator,
    koodi: string,
    laajuus: string
  ): Promise<Locator> {
    const { dialog, kurssiDropdown } =
      await this.avaaLisääOsasuoritusDialogi(oppiaineRivi)
    await kurssiDropdown.filterAndSelectByClick(koodi)

    await dialog
      .getByTestId('laajuus-value')
      .getByTestId('number-editor')
      .fill(laajuus)

    return this.vahvistaLisääOsasuoritusDialogi(oppiaineRivi, dialog, koodi)
  }

  async poistaKurssi(kurssi: Locator): Promise<void> {
    await kurssi.locator('a.remove-value').click()
  }

  kenttä(avain: string, juuri: Locator = this.container): Locator {
    return juuri.getByTestId(`${avain}-value`).first()
  }
}
