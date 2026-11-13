import { Locator, Page, expect } from '@playwright/test'
import { LukioOpiskeluoikeus } from './LukioOpiskeluoikeus'
import { KoskiOppijaPage } from '../KoskiOppijaPage'

/**
 * Yhteinen katselu- ja muokkausrajapinta lukio 2019 -opiskeluoikeuden
 * vanhalle (mallipohjainen editori, lukio/lukio.js) ja uudelle (lukio-v2/,
 * feature flag "lukio-v2") käyttöliittymälle. Mahdollistaa saman testin
 * ajamisen molempia vasten - ks. lukio2019-nayttaminen.spec.ts ja useat
 * muut lukio2019-*.spec.ts-tiedostot, jotka ajavat testejä kummallakin
 * toteutuksella samoilla assertioilla.
 *
 * Osa uuden käyttöliittymän ominaisuuksista (esim. kurssin tietojen
 * muokkausmodaali, paikallisten uudelleenkäyttö) ei ole osa tätä rajapintaa,
 * koska vanhalla käyttöliittymällä ei ole niille vastinetta - ne testataan
 * UusiLukioKatselu-luokan omilla, rajapinnan ulkopuolisilla metodeilla.
 */
export interface LukioKatselu {
  readonly nimi: string
  goto(oid: string): Promise<void>
  readonly koulutusNimi: Locator
  readonly koulutusKoodi: Locator
  readonly opiskeluoikeudenTiedot: Locator
  readonly oppimääräSuoritettu: Locator
  readonly oppimäärä: Locator
  readonly toimipiste: Locator
  readonly suorituskieli: Locator
  readonly ryhmä: Locator
  readonly koulusivistyskieli: Locator
  readonly suoritustyyppiOtsikko: Locator
  readonly oppiaineetOtsikkoArvioitu: Locator
  readonly oppiaineetOtsikkoHyväksytystiArvioitu: Locator
  readonly oppiaineRivit: Locator
  odotaTilaHistoria(rivit: { päivä: string; tila: string }[]): Promise<void>
  omanÄidinkielenOpinnot(): {
    arvosana: Locator
    arviointipäivä: Locator
    kieli: Locator
    laajuus: Locator
  }
  vahvistus(): {
    tila: Locator
    päivä: Locator
    paikka: Locator
    henkilöTeksti: Locator
  }
  oppiaineRivi(koodi: string): Locator
  kurssi(rivi: Locator, koodi: string): Locator
  kurssinArvosana(kurssi: Locator): Locator
  oppiaineenLaajuusArvioitu(rivi: Locator): Locator
  oppiaineenLaajuusHyväksytystiArvioitu(rivi: Locator): Locator
  oppiaineenArvosana(rivi: Locator): Locator
  oppiaineenTilaTitle(rivi: Locator): Locator
  aloitaMuokkaus(): Promise<void>
  // Testioppijan ainoa suoritus on jo "valmis" - arvosanan muutos leimaisi
  // arviointipäivän tämänpäiväiseksi, joka ylittäisi opiskeluoikeuden
  // päättymispäivän. Siksi viimeisin tila poistetaan ennen arvosanaeditointia,
  // kuten vanhan käyttöliittymän lukio2019-kurssit.spec.ts:ssäkin.
  poistaViimeisinTila(): Promise<void>
  // Uusi opiskeluoikeusjakso ("Lisää opiskeluoikeuden tila" -dialogi). Vaatii,
  // että nykyinen viimeisin tila ei ole terminaalitila (ks. poistaViimeisinTila)
  // - dialogin avauslinkki piilotetaan silloin molemmissa käyttöliittymissä.
  lisääTila(tila: string): Promise<void>
  tallenna(): Promise<void>
  muokkaaOppiaineenArvosanaa(rivi: Locator, arvosana: string): Promise<void>
  muokkaaKurssinArvosanaa(kurssi: Locator, arvosana: string): Promise<void>
  oppiaineenNimi(rivi: Locator): Locator
  muokkaaKieliaineenKieltä(rivi: Locator, kieli: string): Promise<void>
  muokkaaOppiaineenSuorituskieltä(rivi: Locator, kieli: string): Promise<void>
  oppiaineenSuorituskieliArvo(rivi: Locator): Locator
  readonly tallennaBtn: Locator
  readonly arvosanaVaadittuVirhe: Locator
  lisääOppiaine(nimi: string): Promise<void>
  poistaOppiaine(rivi: Locator): Promise<void>
  lisääKurssi(rivi: Locator, koodi: string, laajuus: string): Promise<Locator>
  poistaKurssi(kurssi: Locator): Promise<void>
  readonly poistaSuoritusLinkki: Locator
  readonly editBarMessage: Locator
  lisääPaikallinenOppiaine(tiedot: {
    koodi: string
    nimi: string
    kuvaus: string
    arvosana: string
  }): Promise<Locator>
  lisääPaikallinenKurssi(
    rivi: Locator,
    tiedot: { tunniste: string; nimi: string; kuvaus: string; laajuus: string }
  ): Promise<Locator>
  // Ilman riviä: suoritustason lippu (vain "Lukion oppimäärä" -suoritustyypillä).
  // Rivin kanssa: oppiainetason lippu.
  erityinenTutkintoCheckbox(rivi?: Locator): Locator
  pakollinenCheckbox(rivi: Locator): Locator
  laajuusInput(rivi: Locator): Locator
  oppiaineenFootnoteHint(rivi: Locator): Locator
  readonly erityinenTutkintoLegend: Locator
}

export class VanhaLukioKatselu implements LukioKatselu {
  readonly nimi = 'Vanha käyttöliittymä'
  private readonly page: Page
  private readonly otsikko: string | RegExp
  private opiskeluoikeus!: LukioOpiskeluoikeus
  private oppijaPage!: KoskiOppijaPage

  koulutusNimi!: Locator
  koulutusKoodi!: Locator
  opiskeluoikeudenTiedot!: Locator
  oppimääräSuoritettu!: Locator
  oppimäärä!: Locator
  toimipiste!: Locator
  suorituskieli!: Locator
  ryhmä!: Locator
  koulusivistyskieli!: Locator
  suoritustyyppiOtsikko!: Locator
  oppiaineetOtsikkoArvioitu!: Locator
  oppiaineetOtsikkoHyväksytystiArvioitu!: Locator
  oppiaineRivit!: Locator
  tallennaBtn!: Locator
  arvosanaVaadittuVirhe!: Locator
  poistaSuoritusLinkki!: Locator
  editBarMessage!: Locator
  erityinenTutkintoLegend!: Locator

  constructor(page: Page, otsikko: string | RegExp) {
    this.page = page
    this.otsikko = otsikko
  }

  async goto(oid: string) {
    await this.page.goto(`/koski/oppija/${oid}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)
    await this.page
      .getByTestId('opiskeluoikeustyypit-navigation')
      .getByTestId('opiskeluoikeustyyppi-lukiokoulutus')
      .click()

    const container = this.page.locator('.opiskeluoikeus').filter({
      has: this.page.getByRole('heading', { name: this.otsikko })
    })
    this.opiskeluoikeus = new LukioOpiskeluoikeus(container)
    this.oppijaPage = new KoskiOppijaPage(this.page)

    this.koulutusNimi = this.opiskeluoikeus.kenttä('koulutusmoduuli').locator('.tunniste')
    this.koulutusKoodi = this.opiskeluoikeus
      .kenttä('koulutusmoduuli')
      .locator('.tunniste-koodiarvo')
    this.opiskeluoikeudenTiedot = this.opiskeluoikeus.opiskeluoikeudenTiedot
    this.oppimääräSuoritettu = this.opiskeluoikeus.kenttä('oppimääräSuoritettu')
    this.oppimäärä = this.opiskeluoikeus.kenttä('oppimäärä')
    this.toimipiste = this.opiskeluoikeus.kenttä('toimipiste')
    this.suorituskieli = this.opiskeluoikeus.kenttä('suorituskieli')
    this.ryhmä = this.opiskeluoikeus.kenttä('ryhmä')
    this.koulusivistyskieli = this.opiskeluoikeus.kenttä('koulusivistyskieli')
    this.suoritustyyppiOtsikko = this.opiskeluoikeus.container.locator('h3 .koulutus')
    this.oppiaineetOtsikkoArvioitu = this.opiskeluoikeus.oppiaineet.locator(
      'th.laajuus:not(.arvioitu)'
    )
    this.oppiaineetOtsikkoHyväksytystiArvioitu =
      this.opiskeluoikeus.oppiaineet.locator('th.laajuus.arvioitu')
    this.oppiaineRivit = this.opiskeluoikeus.oppiaineet.locator(
      '.oppiaine-rivi'
    )
    this.tallennaBtn = this.oppijaPage.tallennaBtn
    this.arvosanaVaadittuVirhe = this.opiskeluoikeus.oppiaineet.getByText(
      'Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'
    )
    this.poistaSuoritusLinkki = this.opiskeluoikeus.container.getByRole(
      'link',
      { name: 'Poista suoritus' }
    )
    this.editBarMessage = this.oppijaPage.editBarMessage
    this.erityinenTutkintoLegend = this.opiskeluoikeus.container.getByText(
      /erityisenä tutkintona suoritettu oppiaine/i
    )
  }

  // Ei this.opiskeluoikeus.odotaTilaHistoria(): se vertaa label.tila-tekstiä
  // tarkasti (sisältää myös rahoitusmuototeksti suluissa, esim. "Valmistunut
  // (valtionosuusrahoitteinen koulutus)"), kun taas uudessa käyttöliittymässä
  // tila ja rahoitus ovat erillisiä elementtejä - containsText-vertailu pelkällä
  // tila-sanalla toimii kummallekin rakenteelle.
  async odotaTilaHistoria(rivit: { päivä: string; tila: string }[]) {
    const jaksot = this.opiskeluoikeus.kenttä('tila').locator('.opiskeluoikeusjakso')
    for (let i = 0; i < rivit.length; i++) {
      await expect(jaksot.nth(i).locator('label.date')).toHaveText(
        rivit[i].päivä
      )
      await expect(jaksot.nth(i).locator('label.tila')).toContainText(
        rivit[i].tila
      )
    }
  }

  omanÄidinkielenOpinnot() {
    const juuri = this.opiskeluoikeus.omanÄidinkielenOpinnot
    return {
      arvosana: this.opiskeluoikeus.kenttä('arvosana', juuri),
      arviointipäivä: this.opiskeluoikeus.kenttä('arviointipäivä', juuri),
      kieli: this.opiskeluoikeus.kenttä('kieli', juuri),
      laajuus: this.opiskeluoikeus.kenttä('laajuus', juuri)
    }
  }

  vahvistus() {
    const vahvistus = this.opiskeluoikeus.tilaVahvistus
    return {
      tila: vahvistus.locator('.tila'),
      // .date-span on sisäkkäinen (ulompi <span class="date"> kääräisee
      // sisemmän <span class="inline date">) - .first() riittää, molemmat
      // sisältävät saman näkyvän tekstin.
      päivä: vahvistus.locator('.vahvistus.inline .date').first(),
      paikka: vahvistus.locator('.vahvistus.inline .allekirjoitus'),
      henkilöTeksti: vahvistus.locator('.vahvistus.inline')
    }
  }

  oppiaineRivi(koodi: string): Locator {
    return this.opiskeluoikeus.oppiaineRivi(koodi)
  }

  kurssi(rivi: Locator, koodi: string): Locator {
    return this.opiskeluoikeus.kurssi(rivi, koodi)
  }

  kurssinArvosana(kurssi: Locator): Locator {
    return kurssi.locator('.arvosana')
  }

  oppiaineenLaajuusArvioitu(rivi: Locator): Locator {
    return rivi.locator('.laajuus:not(.arvioitu)')
  }

  oppiaineenLaajuusHyväksytystiArvioitu(rivi: Locator): Locator {
    return rivi.locator('.laajuus.arvioitu')
  }

  oppiaineenArvosana(rivi: Locator): Locator {
    return rivi.locator('td.arvosana')
  }

  oppiaineenTilaTitle(rivi: Locator): Locator {
    return rivi.locator('td.suorituksentila')
  }

  async aloitaMuokkaus() {
    await this.oppijaPage.avaaMuokkausnäkymä()
  }

  async poistaViimeisinTila() {
    await this.oppijaPage.poistaViimeisinTila()
  }

  async lisääTila(tila: string) {
    await this.opiskeluoikeus.lisääTila(tila)
  }

  async tallenna() {
    await this.oppijaPage.tallenna()
  }

  async muokkaaOppiaineenArvosanaa(rivi: Locator, arvosana: string) {
    await this.opiskeluoikeus.arvosanaDropdown(rivi).selectOptionByClick(arvosana)
  }

  async muokkaaKurssinArvosanaa(kurssi: Locator, arvosana: string) {
    await this.opiskeluoikeus
      .kurssinArvosanaDropdown(kurssi)
      .selectOptionByClick(arvosana)
  }

  oppiaineenNimi(rivi: Locator): Locator {
    return rivi.locator('.nimi')
  }

  async muokkaaKieliaineenKieltä(rivi: Locator, kieli: string) {
    await this.opiskeluoikeus
      .kieliaineenKieliDropdown(rivi)
      .selectOptionByClick(kieli)
  }

  async muokkaaOppiaineenSuorituskieltä(rivi: Locator, kieli: string) {
    await this.opiskeluoikeus
      .suorituskieliDropdown(rivi)
      .selectOptionByClick(kieli)
  }

  oppiaineenSuorituskieliArvo(rivi: Locator): Locator {
    return this.opiskeluoikeus.suorituskieliDropdown(rivi).textInput
  }

  async lisääOppiaine(nimi: string) {
    await this.opiskeluoikeus.lisääOppiaine(nimi)
  }

  async poistaOppiaine(rivi: Locator) {
    await this.opiskeluoikeus.poistaOppiaine(rivi)
  }

  async lisääKurssi(rivi: Locator, koodi: string, laajuus: string) {
    return this.opiskeluoikeus.lisääValtakunnallinenKurssi(
      rivi,
      koodi,
      laajuus
    )
  }

  async poistaKurssi(kurssi: Locator) {
    await this.opiskeluoikeus.poistaKurssi(kurssi)
  }

  async lisääPaikallinenOppiaine(tiedot: {
    koodi: string
    nimi: string
    kuvaus: string
    arvosana: string
  }) {
    return this.opiskeluoikeus.lisääJaTäytäPaikallinenOppiaine(tiedot)
  }

  async lisääPaikallinenKurssi(
    rivi: Locator,
    tiedot: { tunniste: string; nimi: string; kuvaus: string; laajuus: string }
  ) {
    return this.opiskeluoikeus.lisääPaikallinenKurssi(rivi, tiedot)
  }

  erityinenTutkintoCheckbox(rivi?: Locator) {
    return this.opiskeluoikeus.erityinenTutkintoCheckbox(rivi)
  }

  pakollinenCheckbox(rivi: Locator) {
    return this.opiskeluoikeus.pakollinenCheckbox(rivi)
  }

  laajuusInput(rivi: Locator) {
    return this.opiskeluoikeus.laajuusInput(rivi)
  }

  oppiaineenFootnoteHint(rivi: Locator) {
    return rivi.locator('.title .footnote-hint')
  }
}

export class UusiLukioKatselu implements LukioKatselu {
  readonly nimi = 'Uusi käyttöliittymä'
  private readonly page: Page
  private readonly oid: string | undefined

  koulutusNimi!: Locator
  koulutusKoodi!: Locator
  opiskeluoikeudenTiedot!: Locator
  oppimääräSuoritettu!: Locator
  oppimäärä!: Locator
  toimipiste!: Locator
  suorituskieli!: Locator
  ryhmä!: Locator
  koulusivistyskieli!: Locator
  suoritustyyppiOtsikko!: Locator
  oppiaineetOtsikkoArvioitu!: Locator
  oppiaineetOtsikkoHyväksytystiArvioitu!: Locator
  oppiaineRivit!: Locator
  tallennaBtn!: Locator
  arvosanaVaadittuVirhe!: Locator
  poistaSuoritusLinkki!: Locator
  editBarMessage!: Locator
  erityinenTutkintoLegend!: Locator

  constructor(page: Page) {
    this.page = page
  }

  private suoritus(testId: string): Locator {
    return this.page.getByTestId(`oo.0.suoritukset.0.${testId}`)
  }

  async goto(oid: string) {
    const params = new URLSearchParams({ 'lukio-v2': 'true' })
    await this.page.goto(`/koski/oppija/${oid}?${params.toString()}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)

    this.koulutusNimi = this.suoritus('koulutus')
    this.koulutusKoodi = this.suoritus('koulutuskoodi')
    this.opiskeluoikeudenTiedot = this.page.getByTestId(
      'oo.0.opiskeluoikeus.voimassaoloaika'
    )
    this.oppimääräSuoritettu = this.page.getByTestId(
      'oo.0.opiskeluoikeus.oppimääräSuoritettu.value'
    )
    this.oppimäärä = this.suoritus('oppimäärä.value')
    this.toimipiste = this.suoritus('organisaatio.value')
    this.suorituskieli = this.suoritus('suorituskieli.value')
    this.ryhmä = this.suoritus('ryhmä.value')
    this.koulusivistyskieli = this.suoritus('koulusivistyskieli')
    this.suoritustyyppiOtsikko = this.page.getByTestId('oo.0.suoritusTabs.0.tab')
    this.oppiaineetOtsikkoArvioitu = this.page.locator(
      '.LukionOppiaineet__taulukko th'
    ).nth(2)
    this.oppiaineetOtsikkoHyväksytystiArvioitu = this.page
      .locator('.LukionOppiaineet__taulukko th')
      .nth(3)
    this.oppiaineRivit = this.page.locator(
      '.LukionOppiaineet__taulukko tbody tr'
    )
    this.tallennaBtn = this.page.getByTestId('oo.0.opiskeluoikeus.save')
    this.arvosanaVaadittuVirhe = this.page.getByText(
      'Arvosana vaaditaan, koska päätason suoritus on merkitty valmiiksi.'
    )
    this.poistaSuoritusLinkki = this.page.getByRole('button', {
      name: 'Poista suoritus'
    })
    this.editBarMessage = this.page.getByTestId('oo.0.opiskeluoikeus.editStatus')
    this.erityinenTutkintoLegend = this.page.getByText(
      /erityisenä tutkintona suoritettu oppiaine/i
    )
  }

  async odotaTilaHistoria(rivit: { päivä: string; tila: string }[]) {
    for (let i = 0; i < rivit.length; i++) {
      const base = `oo.0.opiskeluoikeus.tila.value.items.${i}`
      await expect(this.page.getByTestId(`${base}.date`)).toHaveText(
        rivit[i].päivä
      )
      await expect(this.page.getByTestId(`${base}.tila`)).toContainText(
        rivit[i].tila
      )
    }
  }

  omanÄidinkielenOpinnot() {
    return {
      arvosana: this.suoritus('omanÄidinkielenOpinnot.arvosana'),
      arviointipäivä: this.suoritus('omanÄidinkielenOpinnot.arviointipäivä.value'),
      kieli: this.suoritus('omanÄidinkielenOpinnot.kieli.value'),
      laajuus: this.suoritus('laajuus.value')
    }
  }

  vahvistus() {
    const details = this.suoritus('suorituksenVahvistus.value.details')
    return {
      tila: this.suoritus('suorituksenVahvistus.value.status'),
      // Uudessa käyttöliittymässä päivä ja paikka ovat samassa tekstissä
      // ("Vahvistus: 15.5.2020 Jyväskylä") - ei erillisiä elementtejä kuten
      // vanhassa. containsText-tyylinen vertailu riittää molemmille.
      päivä: details,
      paikka: details,
      henkilöTeksti: this.suoritus('suorituksenVahvistus.value.henkilö.0')
    }
  }

  oppiaineRivi(koodi: string): Locator {
    return this.page.locator(`[data-koodiarvo="${koodi}"]`)
  }

  kurssi(rivi: Locator, koodi: string): Locator {
    return rivi.locator('li.kurssi').filter({
      has: this.page.locator('.tunniste', { hasText: new RegExp(`^${koodi}$`) })
    })
  }

  kurssinArvosana(kurssi: Locator): Locator {
    return kurssi.locator('.arvosana')
  }

  oppiaineenLaajuusArvioitu(rivi: Locator): Locator {
    return rivi.locator('.LukionOppiaineet__laajuus').nth(0)
  }

  oppiaineenLaajuusHyväksytystiArvioitu(rivi: Locator): Locator {
    return rivi.locator('.LukionOppiaineet__laajuus').nth(1)
  }

  oppiaineenArvosana(rivi: Locator): Locator {
    return rivi.locator('.LukionOppiaineet__arvosana')
  }

  oppiaineenTilaTitle(rivi: Locator): Locator {
    return rivi.locator('.LukionOppiaineet__tila [title]')
  }

  async aloitaMuokkaus() {
    await this.page.getByTestId('oo.0.opiskeluoikeus.edit').click()
  }

  async poistaViimeisinTila() {
    // Vain uusimmalla (isLatest) jaksolla on poistopainike, joten sen
    // testitunnisteen indeksi ei ole kiinteä (riippuu jakson alkuperäisestä
    // paikasta tila.opiskeluoikeusjaksot-taulukossa) - haetaan attribuutin
    // osalla, ei kiinteällä indeksillä.
    await this.page
      .locator(
        '[data-testid^="oo.0.opiskeluoikeus.tila.edit.items."][data-testid$=".remove"]'
      )
      .click()
  }

  async lisääTila(tila: string) {
    await this.page.getByTestId('oo.0.opiskeluoikeus.tila.edit.add').click()
    await this.page.locator(`label[for*="${tila.toLowerCase()}"]`).click()
    await this.page
      .getByTestId('oo.0.opiskeluoikeus.tila.edit.modal.submit')
      .click()
  }

  async tallenna() {
    await this.page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(this.page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible(
      { timeout: 15000 }
    )
  }

  private async valitseDropdownArvo(toggle: Locator, arvo: string) {
    await toggle.click()
    await this.page
      .locator('.Select__optionLabel')
      .filter({ hasText: new RegExp(`^${arvo}$`) })
      .click()
  }

  async muokkaaOppiaineenArvosanaa(rivi: Locator, arvosana: string) {
    await this.valitseDropdownArvo(
      rivi.locator('.LukionOppiaineet__arvosana .Select__toggle'),
      arvosana
    )
  }

  // Arvosana muokataan kurssin tietojen muokkausmodaalin kautta (ks.
  // avaaKurssinMuokkausModaali) - rivillä näkyy siitä aina vain katselu
  // (ks. LukionOppiaineet.tsx:n KurssinTietojenMuokkausModaali).
  async muokkaaKurssinArvosanaa(kurssi: Locator, arvosana: string) {
    const modaali = await this.avaaKurssinMuokkausModaali(kurssi)
    await this.valitseDropdownArvo(
      modaali.locator('[data-testid$="arvosana.edit"] .Select__toggle'),
      arvosana
    )
    await this.suljeModaali(modaali)
  }

  oppiaineenNimi(rivi: Locator): Locator {
    return rivi.locator('.LukionOppiaineet__nimi')
  }

  async muokkaaKieliaineenKieltä(rivi: Locator, kieli: string) {
    await this.valitseDropdownArvo(
      rivi.locator('.LukionOppiaineet__nimi .Select__toggle'),
      kieli
    )
  }

  async muokkaaOppiaineenSuorituskieltä(rivi: Locator, kieli: string) {
    await this.valitseDropdownArvo(
      rivi.locator('.LukionOppiaineet__suorituskieli .Select__toggle'),
      kieli
    )
  }

  oppiaineenSuorituskieliArvo(rivi: Locator): Locator {
    return rivi.locator('.LukionOppiaineet__suorituskieli .Select__input')
  }

  // Suorituskieli, Pakollinen, Tunnustettu ja Arvosana ovat muokattavissa vain
  // kurssin tietojen muokkausmodaalin kautta (klikkaus kurssin tunnisteeseen
  // muokkaustilassa) - ei enää rivin omana muokattavana kenttänä (rivillä
  // näkyy Arvosanasta silti aina pelkkä katselu, ks. LukionOppiaineet.tsx:n
  // KurssinTietojenMuokkausModaali). Julkinen (ei vain muokkaaKurssinSuoritus-
  // kieltä/kurssinPakollinenCheckbox:n sisäinen apu), koska myös
  // lukio2019-moduulin-lisays-ja-poisto.spec.ts avaa modaalin suoraan
  // Laajuuden/Arviointipäivän/Kuvauksen muokkausta varten.
  async avaaKurssinMuokkausModaali(kurssi: Locator): Promise<Locator> {
    const modaali = this.page.locator('[role="dialog"]')
    // Idempotentti: modaali peittää koko kurssirivin, joten toinen klikkaus
    // tunnisteeseen sen ollessa jo auki estyisi (modaali sieppaisi klikkauksen)
    // - esim. kun samalle kurssille kutsutaan sekä kurssinPakollinenCheckbox
    // että muokkaaKurssinSuorituskieltä peräkkäin.
    if ((await modaali.count()) === 0) {
      await kurssi.locator('.tunniste').click()
      await expect(modaali).toBeVisible()
      // Modal.tsx fokusoi automaattisesti ensimmäisen fokusoitavan kentän
      // auetessaan, ja fokus näytti livenä palautuvan takaisin bodyyn vielä
      // hetken päästä siitäkin riippumatta (toistettavissa myös ilman mitään
      // omaa vuorovaikutusta - ei siis luotettava "asettuiko modaali jo"
      // -signaali, todennäköisesti tämän Playwright/selainympäristön oma
      // kvirkki eikä tuotantokoodin bugi). Odotetaan silti hetki ennen
      // ensimmäistä täyttöä, koska ensimmäinen vuorovaikutus heti auetessa
      // hävisi kerran livenä testatessa (Kuvaus palautui alkuperäiseksi
      // arvoksi välittömästi täytön jälkeen).
      await this.page.waitForTimeout(300)
    }
    return modaali
  }

  private async suljeModaali(modaali: Locator) {
    await modaali.getByRole('button', { name: 'Sulje' }).click()
  }

  // Ei vastinetta LukioKatselu-rajapinnassa eikä VanhaLukioKatselussa - uusi,
  // ei portattu ominaisuus (ks. lukio2019-moduulin-lisays-ja-poisto.spec.ts).
  async muokkaaKurssinSuorituskieltä(kurssi: Locator, kieli: string) {
    const modaali = await this.avaaKurssinMuokkausModaali(kurssi)
    await this.valitseDropdownArvo(
      modaali.locator('[data-testid$="suorituskieli.edit.toggle"]'),
      kieli
    )
    await this.suljeModaali(modaali)
  }

  // Ei vastinetta LukioKatselu-rajapinnassa eikä VanhaLukioKatselussa - sama
  // peruste kuin muokkaaKurssinSuorituskieltä:ssä. Palauttaa Promisen, koska
  // modaali pitää avata ensin - ei siis sama synkroninen allekirjoitus kuin
  // jaetun rajapinnan oppiainetason pakollinenCheckbox():lla.
  async kurssinPakollinenCheckbox(kurssi: Locator): Promise<Locator> {
    const modaali = await this.avaaKurssinMuokkausModaali(kurssi)
    return modaali
      .locator('[data-testid$="pakollinen.edit"]')
      .getByRole('checkbox')
  }

  async lisääOppiaine(nimi: string) {
    await this.valitseDropdownArvo(
      this.suoritus('uusi-oppiaine.toggle'),
      nimi
    )
  }

  async poistaOppiaine(rivi: Locator) {
    await rivi.locator('[data-testid$=".poisto"]').click()
  }

  async lisääKurssi(rivi: Locator, koodi: string, laajuus: string) {
    await rivi.getByText('Lisää osasuoritus').click()

    const dialog = this.page.locator('[role="dialog"]')
    await dialog.locator('.Select__toggle').click()
    await dialog.locator('.Select__input').fill(koodi)
    await this.page
      .locator('.Select__optionLabel')
      .filter({ hasText: new RegExp(`^${koodi}\\b`) })
      .first()
      .click()
    await dialog.locator('.LaajuusField input').fill(laajuus)
    await dialog.getByRole('button', { name: 'Lisää' }).click()

    return this.kurssi(rivi, koodi)
  }

  async poistaKurssi(kurssi: Locator) {
    await kurssi.locator('[data-testid$=".poisto"]').click()
  }

  async lisääPaikallinenOppiaine(tiedot: {
    koodi: string
    nimi: string
    kuvaus: string
    arvosana: string
  }) {
    await this.valitseDropdownArvo(
      this.suoritus('uusi-oppiaine.toggle'),
      'Lisää...'
    )

    const rivi = this.page
      .locator('.LukionOppiaineet__taulukko tbody tr')
      .last()
    await rivi
      .locator('[data-testid$=".paikallinenKoodi.edit.input"]')
      .fill(tiedot.koodi)
    await rivi
      .locator('[data-testid$=".paikallinenNimi.edit.input"]')
      .fill(tiedot.nimi)
    await rivi
      .locator('[data-testid$=".paikallinenKuvaus.edit.input"]')
      .fill(tiedot.kuvaus)
    await this.muokkaaOppiaineenArvosanaa(rivi, tiedot.arvosana)

    return this.oppiaineRivi(tiedot.koodi)
  }

  async lisääPaikallinenKurssi(
    rivi: Locator,
    tiedot: { tunniste: string; nimi: string; kuvaus: string; laajuus: string }
  ) {
    await rivi.getByText('Lisää osasuoritus').click()

    const dialog = this.page.locator('[role="dialog"]')
    await dialog.locator('.Select__toggle').click()
    // Toisin kuin valtakunnallisen moduulin valinnassa, tässä ei haeta
    // koodilla vaan kiinteällä sanalla "paikallinen" - sama "Lisää paikallinen
    // kurssi..." -vaihtoehto löytyy sillä aina, oli lisättävän kurssin tunniste
    // mikä tahansa.
    await dialog.locator('.Select__input').fill('paikallinen')
    await this.page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Lisää paikallinen kurssi\.\.\.$/ })
      .click()

    await dialog
      .locator('[data-testid$=".paikallinenKoodi.input"]')
      .fill(tiedot.tunniste)
    await dialog
      .locator('[data-testid$=".paikallinenNimi.input"]')
      .fill(tiedot.nimi)
    await dialog
      .locator('[data-testid$=".paikallinenKuvaus.input"]')
      .fill(tiedot.kuvaus)
    await dialog.locator('.LaajuusField input').fill(tiedot.laajuus)
    await dialog.getByRole('button', { name: 'Lisää' }).click()

    return this.kurssi(rivi, tiedot.tunniste)
  }

  erityinenTutkintoCheckbox(rivi?: Locator) {
    return rivi
      ? rivi
          .locator('[data-testid$=".suoritettuErityisenäTutkintona.edit"]')
          .getByRole('checkbox')
      : this.suoritus('suoritettuErityisenäTutkintona.edit').getByRole(
          'checkbox'
        )
  }

  // .first(), koska sama testid-pääte ("pakollinen.edit") toistuu oppiaineen
  // omassa kentässä JA jokaisen sen kurssin omassa kentässä - DOM-järjestyksessä
  // oppiaineen oma kenttä tulee aina ennen kurssien kenttiä, sama periaate kuin
  // LukioOpiskeluoikeus.kenttä():ssä.
  pakollinenCheckbox(rivi: Locator) {
    return rivi
      .locator('[data-testid$="pakollinen.edit"]')
      .getByRole('checkbox')
      .first()
  }

  laajuusInput(rivi: Locator) {
    return rivi.locator('.LukionOppiaineet__laajuus').first().locator('.LaajuusField input')
  }

  oppiaineenFootnoteHint(rivi: Locator) {
    return rivi.locator('.footnote-hint')
  }
}
