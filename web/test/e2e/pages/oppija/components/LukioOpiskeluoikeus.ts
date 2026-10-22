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

  constructor(container: Locator) {
    this.container = container
    this.tutkinnonNimi = this.kenttä('koulutusmoduuli').locator('.tunniste')
    this.opiskeluoikeudenTiedot = this.container.locator(
      '.opiskeluoikeuden-tiedot'
    )
    this.omanÄidinkielenOpinnot = this.kenttä('omanÄidinkielenOpinnot')
    this.tilaVahvistus = this.container.locator('.suoritus > .tila-vahvistus')
    // Oppiaineiden/kurssien taulukko on oma React-komponenttinsa, ei
    // PropertiesEditorin kautta renderöity - ei siis data-testid:tä.
    // Sama taulukkoluokka on yhteinen "Lukion oppimäärä"- ja
    // "Lukion oppiaineiden oppimäärien suoritus" -suoritustyypeille.
    this.oppiaineet = this.container.locator('table.suoritukset.oppiaineet')
  }

  oppiaineRivi(koodi: string): Locator {
    return this.oppiaineet.locator(`.oppiaine-rivi.${koodi}`)
  }

  // Kurssi tunnistetaan sen näkyvästä koodista (esim. "ÄI1") roolin ja nimen
  // kautta, ei sijainnista listassa - kurssien järjestys ei ole osa
  // testattavaa käyttäytymistä.
  kurssi(oppiaineRivi: Locator, koodi: string): Locator {
    return oppiaineRivi.locator('li.kurssi').filter({
      has: oppiaineRivi.page().getByRole('button', { name: koodi, exact: true })
    })
  }

  // Kieliaineen kieli (esim. "Suomen kieli ja kirjallisuus") ei ole oma
  // PropertiesEditor-rivi, vaan osa oppiaineen otsikkoa - ei data-testid:tä
  // ympärillä, CSS-skoppaus sama kuin vanhassa Mocha-testissä.
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

  arvosanaDropdown(oppiaineRivi: Locator): Dropdown {
    return new Dropdown(
      oppiaineRivi.page(),
      oppiaineRivi.locator('td.arvosana').getByTestId('enum-editor-dropdown')
    )
  }

  // "Lisää oppiaine" -pudotusvalikko lataa koodiston asynkronisesti
  // (ajax-loading-placeholder) - odotetaan sen valmistumista ennen valintaa.
  async lisääOppiaine(nimi: string): Promise<void> {
    const uusiOppiaine = this.container.locator('.uusi-oppiaine').first()
    await expect(
      uusiOppiaine.locator('.ajax-loading-placeholder')
    ).toHaveCount(0)
    const dropdown = new Dropdown(
      uusiOppiaine.page(),
      uusiOppiaine.locator('.dropdown')
    )
    await dropdown.selectOptionByClick(nimi)
  }

  // Poistolinkillä ei ole näkyvää tekstiä, aria-labelia eikä title-attribuuttia
  // - CSS on ainoa toimiva valinta, ei roolin/nimen puutteen vuoksi oikotie.
  async poistaOppiaine(oppiaineRivi: Locator): Promise<void> {
    await oppiaineRivi.locator('td.remove-row a').click()
  }

  // "Lisää..." on sama pudotusvalikko kuin lisääOppiaine(), mutta oma
  // erikoisvaihtoehtonsa luo tyhjän paikallisen oppiaineen täytettäväksi,
  // ei valitse olemassaolevaa koodiston arvoa.
  async lisääPaikallinenOppiaine(): Promise<Locator> {
    const uusiOppiaine = this.container.locator('.uusi-oppiaine').first()
    await expect(
      uusiOppiaine.locator('.ajax-loading-placeholder')
    ).toHaveCount(0)
    const dropdown = new Dropdown(
      uusiOppiaine.page(),
      uusiOppiaine.locator('.dropdown')
    )
    await dropdown.selectOptionByClick('Lisää...')
    return this.oppiaineet.locator('.oppiaine-rivi').last()
  }

  paikallisenOppiaineenKoodi(oppiaineRivi: Locator): Locator {
    return oppiaineRivi.getByPlaceholder('Koodi')
  }

  paikallisenOppiaineenNimi(oppiaineRivi: Locator): Locator {
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

  // "Lisää osasuoritus" -linkki avaa valintaikkunan (role="dialog"), jossa
  // ensin valitaan kurssi pudotusvalikosta, täytetään sen kentät ja
  // vahvistetaan. Jaettu lisääPaikallinenKurssi()/lisääValtakunnallinenKurssi()
  // -metodien kesken.
  private async avaaLisääOsasuoritusDialogi(
    oppiaineRivi: Locator
  ): Promise<{ dialog: Locator; kurssiDropdown: Dropdown }> {
    await oppiaineRivi.getByText('Lisää osasuoritus').click()
    const page = oppiaineRivi.page()
    const dialog = page.getByRole('dialog')
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

  // Tunniste/nimi/laajuus/kuvaus ovat tavallisia PropertiesEditor-rivejä,
  // joten data-testid toimii suoraan.
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

  // Valtakunnallinen moduuli ei näy pudotusvalikon oletuslistassa (katkaistu/
  // rajattu alijoukko) - Dropdown.filterAndSelectByClick() kirjoittaa koodin
  // hakukenttään ja klikkaa suodatettua vaihtoehtoa sen koodin perusteella
  // (ei koko näkyvää nimeä, jotta koodiston sanamuodon muutos ei riko testiä).
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

  // Poistolinkki on sama rakenne kuin oppiaineen remove-value, ei roolia/nimeä.
  async poistaKurssi(kurssi: Locator): Promise<void> {
    await kurssi.locator('a.remove-value').click()
  }

  // Yleinen PropertiesEditor (web/app/editor/PropertiesEditor.jsx) merkitsee joka
  // skeemakentän arvon data-testid:llä "<kenttäavain>-value" - käytetään sitä
  // suoraan CSS-luokkien sijaan. .first(), koska sama kenttäavain (esim.
  // arvosana) voi toistua sisäkkäisissä osasuorituksissa; DOM-järjestyksessä
  // oma kenttä tulee aina ennen sisäkkäisiä.
  kenttä(avain: string, juuri: Locator = this.container): Locator {
    return juuri.getByTestId(`${avain}-value`).first()
  }
}
