import { Locator, Page } from '@playwright/test'
import { expect } from '../../base'

export class KoskiRaportitPage {
  readonly muodostaButton: Locator
  readonly taustallaOhje: Locator
  readonly raportit: Locator
  readonly raporttiRivit: Locator

  constructor(private readonly page: Page) {
    this.muodostaButton = page.getByTestId('massaluovutus.start')
    this.taustallaOhje = page.getByTestId('massaluovutus.taustalla')
    this.raportit = page.getByTestId('massaluovutus.raportit')
    this.raporttiRivit = this.raportit.locator('tbody tr')
  }

  async gotoAmmatillinenSuoritustiedot() {
    await this.page.goto(
      '/koski/raportit/ammatillinen-koulutus/ammatillinentutkintosuoritustietojentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoAmmatillinenOsittainenSuoritustiedot() {
    await this.page.goto(
      '/koski/raportit/ammatillinen-koulutus/ammatillinenosittainensuoritustietojentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoAmmatillinenOpiskelijavuositiedot() {
    await this.page.goto(
      '/koski/raportit/ammatillinen-koulutus/ammatillinenopiskelijavuositiedot'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoMuuAmmatillinen() {
    await this.page.goto(
      '/koski/raportit/ammatillinen-koulutus/muuammatillinenkoulutus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoTopksAmmatillinen() {
    await this.page.goto(
      '/koski/raportit/ammatillinen-koulutus/topksammatillinen'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoMuks() {
    await this.page.goto(
      '/koski/raportit/muks/muunkuinsäännellynkoulutuksenraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoTuvaSuoritustiedot() {
    await this.page.goto('/koski/raportit/tuva/tuvasuoritustietojentarkistus')
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoAikuistenPerusopetuksenKurssikertyma() {
    await this.page.goto(
      '/koski/raportit/aikuisten-perusopetus/aikuistenperusopetuskurssikertymänraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoLukionKurssikertymat() {
    await this.page.goto('/koski/raportit/lukio/lukiokurssikertyma')
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoLukio2019Opintopistekertymat() {
    await this.page.goto('/koski/raportit/lukio2019/lukioopintopistekertyma')
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoPerusopetukseenValmistava() {
    await this.page.goto(
      '/koski/raportit/perusopetukseenvalmistava/perusopetukseenvalmistavanopetuksentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoLukionSuoritustiedot() {
    await this.page.goto(
      '/koski/raportit/lukio/lukionsuoritustietojentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoLukio2019Suoritustiedot() {
    await this.page.goto(
      '/koski/raportit/lukio2019/lukionsuoritustietojentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoPerusopetuksenOppijamaarat() {
    await this.page.goto(
      '/koski/raportit/perusopetus/perusopetuksenoppijamääräraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoPerusopetuksenLisaopetuksenOppijamaarat() {
    await this.page.goto(
      '/koski/raportit/perusopetus/perusopetuksenlisäopetuksenoppijamääräraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoTuvaPerusopetuksenOppijamaarat() {
    await this.page.goto(
      '/koski/raportit/tuva/tuvaperusopetuksenoppijamääräraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoAikuistenPerusopetuksenOppijamaarat() {
    await this.page.goto(
      '/koski/raportit/aikuisten-perusopetus/aikuistenperusopetusoppijamäärienraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoLukiokoulutuksenOpiskelijamaarat() {
    await this.page.goto(
      '/koski/raportit/lukio/lukiodiaibinternationaleshopiskelijamaarat'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoLuvaOpiskelijamaarat() {
    await this.page.goto('/koski/raportit/lukio/luvaopiskelijamaarat')
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoPerusopetuksenVuosiluokka() {
    await this.page.goto(
      '/koski/raportit/perusopetus/perusopetuksenvuosiluokka'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoAikuistenPerusopetusSuoritustiedot() {
    await this.page.goto(
      '/koski/raportit/aikuisten-perusopetus/aikuistenperusopetussuoritustietojentarkistus'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoIBSuoritustiedot() {
    await this.page.goto('/koski/raportit/ib/ibsuoritustietojentarkistus')
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoPaallekkaisetOpiskeluoikeudet() {
    await this.page.goto('/koski/raportit/muut/paallekkaisetopiskeluoikeudet')
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoEsiopetuksenOppijamaarat() {
    await this.page.goto(
      '/koski/raportit/esiopetus/esiopetuksenoppijamäärienraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async valitseOrganisaatio(nimi: string) {
    const valinta = this.page.locator('.organisaatio-dropdown')
    await valinta.locator('input.select').click()
    await valinta.getByText(nimi, { exact: true }).click()
    await expect(valinta.locator('input.select')).toHaveValue(nimi)
  }

  async gotoVstJotpa() {
    await this.page.goto(
      '/koski/raportit/vst/jatkuvanoppimisenvapaansivistystyönraportti'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoVstVapaatavoitteinen() {
    await this.page.goto(
      '/koski/raportit/vst/vapaansivistystyönvapaatavoitteisenkoulutuksenraportti?vstraportit=1'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoVstKops() {
    await this.page.goto(
      '/koski/raportit/vst/vapaansivistystyönoppivelvollisillesuunnatunkoulutuksenraportti?vstraportit=1'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async gotoVstOsaamismerkit() {
    await this.page.goto(
      '/koski/raportit/vst/vapaansivistystyönosaamismerkkienraportti?vstraportit=1'
    )
    await expect(this.muodostaButton).toBeVisible()
  }

  async syötäAikajakso(alku: string, loppu: string) {
    await this.page.locator('#dateinput-alku').fill(alku)
    await this.page.locator('#dateinput-loppu').fill(loppu)
    await expect(this.muodostaButton).toBeEnabled()
  }

  async syötäPäivä(pvm: string) {
    await this.page.locator('#dateinput-paiva').fill(pvm)
    await expect(this.muodostaButton).toBeEnabled()
  }

  async valitseRaportinTyyppi(nimi: string) {
    const vaihtoehto = this.page.locator('label.radio-option-container', {
      hasText: nimi
    })
    await vaihtoehto.click()
    await expect(vaihtoehto).toHaveAttribute('data-selected', 'true')
  }

  async rajaaOsasuorituksetAikajaksolle() {
    const vaihtoehto = this.page.locator('label.radio-option-container', {
      hasText: 'Raportille valitaan vain sellaiset'
    })
    await vaihtoehto.click()
    await expect(vaihtoehto).toHaveAttribute('data-selected', 'true')
  }

  async syötäKotikuntaPvm(pvm: string) {
    await this.page.locator('#dateinput-kotikuntapvm').fill(pvm)
  }

  async valitseVuosiluokka(vuosiluokka: string) {
    const valinta = this.page.locator('.parametri.vuosiluokka')
    await valinta.locator('.select').click()
    await valinta
      .getByRole('listitem', { name: vuosiluokka, exact: true })
      .click()
    await expect(valinta.locator('.select')).toHaveText(vuosiluokka)
  }

  async muodosta() {
    await this.muodostaButton.click()
  }

  async muodostaJaLueKysely() {
    const pyyntö = this.page.waitForRequest(
      (r) =>
        r.method() === 'POST' &&
        new URL(r.url()).pathname === '/koski/api/massaluovutus'
    )
    await this.muodosta()
    return (await pyyntö).postDataJSON()
  }

  latauslinkki(rivi: Locator): Locator {
    return rivi.getByRole('link', { name: 'Lataa Excel-tiedosto' })
  }

  salasana(rivi: Locator): Locator {
    return rivi.locator('td.password')
  }

  kotikuntaPvm(rivi: Locator): Locator {
    return rivi.locator('td.kotikunta')
  }

  vuosiluokka(rivi: Locator): Locator {
    return rivi.locator('td.vuosiluokka')
  }

  raportinTyyppi(rivi: Locator): Locator {
    return rivi.locator('td.raportin-tyyppi')
  }
}
