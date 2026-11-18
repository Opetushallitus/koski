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
    // Oppiaineiden/kurssien taulukko on oma React-komponenttinsa, ei
    // PropertiesEditorin kautta renderöity - ei siis data-testid:tä.
    // Sama taulukkoluokka on yhteinen "Lukion oppimäärä"- ja
    // "Lukion oppiaineiden oppimäärien suoritus" -suoritustyypeille.
    this.oppiaineet = this.container.locator('table.suoritukset.oppiaineet')
    // Ei data-testid:tä, ei roolia/nimeä - ei PropertiesEditor-rivi.
    this.päättymispäivä = this.container.locator('.päättymispäivä')
  }

  oppiaineRivi(koodi: string): Locator {
    return this.oppiaineet.locator(`.oppiaine-rivi.${koodi}`)
  }

  // Tila-historia on lista erillisiä opiskeluoikeusjaksoja: varmistetaan
  // päivämäärä ja tila per jakso erikseen, jotta pari ei voi mennä sekaisin.
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

  // "Lisää opiskeluoikeuden tila" -linkki on <a> ilman href-attribuuttia -
  // ei siis linkki-roolia, testid on roolin puutteessa parempi valinta kuin
  // CSS-luokkaketju. Avautuva valintaikkuna tarjoaa tilan roolipohjaisena
  // radiopainikeryhmänä.
  async lisääTila(tila: string): Promise<void> {
    await this.container
      .getByTestId('lisää-opiskeluoikeuden-tila-btn')
      .click()
    const dialog = this.container.page().getByRole('dialog')
    await dialog.getByRole('radio', { name: tila }).click()
    await dialog.getByTestId('dialog-vahvista').click()
  }

  // Kurssi tunnistetaan sen näkyvästä koodista (esim. "ÄI1") roolin ja nimen
  // kautta, ei sijainnista listassa - kurssien järjestys ei ole osa
  // testattavaa käyttäytymistä.
  kurssi(oppiaineRivi: Locator, koodi: string): Locator {
    return oppiaineRivi.locator('li.kurssi').filter({
      has: oppiaineRivi.page().getByRole('button', { name: koodi, exact: true })
    })
  }

  // Kieliaineen kieli ei ole oma PropertiesEditor-rivi, vaan osa oppiaineen
  // otsikkoa - ei data-testid:tä ympärillä.
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

  // "suoritettuErityisenäTutkintona" on additionalOnlyEditableProperties -
  // näkyy vain muokkaustilassa, ei koskaan katselutilassa edes arvolla true;
  // katselussa sen näkyvä vaikutus on "**"-alaviite (footnote-hint), ei oma
  // tekstikenttä. Toimii sekä suoritus- että oppiainetasolla - juuri valitsee
  // kumman. Huom: suoritustason lipun asettaminen vaikuttaa KAIKKIIN
  // oppiaineisiin (isErityinenTutkinto OR:ttaa suoritus- ja oppiainetason),
  // ja jokaiselta vaaditaan tämän jälkeen oma laajuus - turvallinen testata
  // vain tyhjällä, juuri luodulla suorituksella, ei valmiiksi täytetyllä.
  erityinenTutkintoCheckbox(juuri: Locator = this.container): Locator {
    return this.kenttä('suoritettuErityisenäTutkintona', juuri).getByRole(
      'checkbox'
    )
  }

  // Laajuus muuttuu muokattavaksi inline-syötteeksi ainoastaan erityisenä
  // tutkintona suoritetulle oppiaineelle (LukionOppiaineEditor.jsx:
  // shouldEditLaajuus) - muulloin laajuus lasketaan kursseista eikä sitä voi
  // syöttää suoraan. Sama "number-editor"-testid kuin muualla tässä
  // tiedostossa laajuuden numerosyötteillä (lisääPaikallinenKurssi ym.).
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

  // "Lisää oppiaine" -pudotusvalikko lataa koodiston asynkronisesti
  // (ajax-loading-placeholder) - odotetaan sen valmistumista ennen valintaa.
  // Pudotusvalikon kehyksellä itsellään ei ole data-testid:tä, mutta sen
  // combobox-syötteellä on näkyvä nimi (placeholder "Lisää oppiaine") -
  // fromComboboxName tunnistaa kehyksen sen perusteella, ei kehyskohtaisella
  // CSS-luokalla.
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

  // "Lisää..." luo tyhjän paikallisen oppiaineen täytettäväksi, ei valitse
  // olemassaolevaa koodiston arvoa.
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
    // Dialogin kurssipudotusvalikko lataa koodiston asynkronisesti, samoin
    // kuin uusiOppiaineDropdown() - odotetaan latauksen valmistumista ennen
    // kuin pudotusvalikkoon koskee, muuten combobox ei ole vielä olemassa.
    await expect(dialog.locator('.ajax-loading-placeholder')).toHaveCount(0)
    // Ei Dropdown.fromComboboxName(): tämän combobox'in aria-label seuraa
    // elävästi kirjoitettua hakutekstiä (Dropdown.jsx: aria-label on
    // queryAtomista riippuvainen) - kun filterAndSelectByClick() kirjoittaa
    // kenttään, nimi "Lisää osasuoritus" katoaa ja laiska has-suodatin lakkaa
    // täsmäämästä kesken haun. Varmistettu livenä: kirjoituksen jälkeen
    // nimihaku ei enää löydä combobox'ia lainkaan.
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

  // Samantyyppinen tilanne kuin poistaOppiaine(): ei roolia/nimeä.
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
