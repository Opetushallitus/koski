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

  async rajaaOsasuorituksetAikajaksolle() {
    const vaihtoehto = this.page.locator('label.radio-option-container', {
      hasText: 'Raportille valitaan vain sellaiset'
    })
    await vaihtoehto.click()
    await expect(vaihtoehto).toHaveAttribute('data-selected', 'true')
  }

  async muodosta() {
    await this.muodostaButton.click()
  }

  latauslinkki(rivi: Locator): Locator {
    return rivi.getByRole('link', { name: 'Lataa Excel-tiedosto' })
  }

  salasana(rivi: Locator): Locator {
    return rivi.locator('td.password')
  }
}
