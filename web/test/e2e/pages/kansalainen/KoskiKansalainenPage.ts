import { Locator, Page } from '@playwright/test'
import { YoTodistusLanguage } from '../../../../app/components-v2/yotutkinto/YoTodistus'
import { expect } from '../../base'
import { build, BuiltIdNode } from '../oppija/uiV2builder/builder'
import { Button } from '../oppija/uiV2builder/Button'
import { Label } from '../oppija/uiV2builder/Label'
import { Select } from '../oppija/uiV2builder/Select'
import { KansalainenOpiskeluoikeusHeader } from '../oppija/uiV2builder/kansalainen/KansalainenOpiskeluoikeusHeader'
import { SuoritusotePage } from './SuoritusotePage'

export class KoskiKansalainenPage {
  $: BuiltIdNode<KansalainenUIV2TestIds>
  peruSuostumusLinkki: Locator

  constructor(private readonly page: Page) {
    this.$ = build(page, KansalainenUIV2TestIds)
    this.peruSuostumusLinkki = page.locator('.peru-suostumus-linkki')
  }

  static create(page: Page) {
    return new this(page)
  }

  async goto() {
    await this.page.goto(`/koski/omattiedot`)
    await expect(this.page).toHaveURL(/\/koski\/omattiedot$/)
  }

  async openOppija(oid: string) {
    this.page.getByTestId('oppijanvalitsin-dropdown').selectOption(oid)
  }

  async openOpiskeluoikeus(name: string) {
    await this.page.getByRole('button', { name }).click()
  }

  async setYoTodistusLanguage(lang: YoTodistusLanguage) {
    await this.$.yoTodistus.language.set(lang)
  }

  async generateYoTodistus() {
    await this.$.yoTodistus.start.click()
    await this.$.yoTodistus.loading.waitFor()
    await this.$.yoTodistus.loading.waitForToDisappear(10000)
  }

  async getYoTodistusError(): Promise<string> {
    return await this.$.yoTodistus.error.value()
  }

  async getYoTodistusFile() {
    const popupPromise = this.page.waitForEvent('popup')
    await this.$.yoTodistus.open.click()
    const popup = await popupPromise
    await popup.waitForLoadState('networkidle')
    await popup.close()
  }

  async openJaaSuoritustietoja() {
    await this.page
      .getByRole('button', { name: /Sulje Suoritustietojen jakaminen/ })
      .isHidden()
    await this.page.getByRole('button', { name: /Jaa suoritustietoja/ }).click()
    await this.page
      .getByRole('button', { name: /Sulje Suoritustietojen jakaminen/ })
      .isVisible()
  }

  async closeJaaSuoritustietoja() {
    await this.page
      .getByRole('button', { name: /Sulje Suoritustietojen jakaminen/ })
      .isVisible()
    await this.page
      .getByRole('button', { name: /Sulje Suoritustietojen jakaminen/ })
      .click()
    await this.page
      .getByRole('button', { name: /Sulje Suoritustietojen jakaminen/ })
      .isHidden()
  }

  suoritustietoLabel(
    oppilaitosOid: string,
    ptsTyyppi: string,
    koulutusmoduuli: string
  ) {
    return this.page.getByTestId(
      `__${oppilaitosOid}__${ptsTyyppi}__${koulutusmoduuli}__label`
    )
  }

  jaaValitsemasiOpinnotButton() {
    return this.page.getByRole('button', { name: 'Jaa valitsemasi opinnot' })
  }

  katsoSuoritusoteLink() {
    return this.page.getByRole('link', {
      name: 'Katso, miltä suoritusote näyttää selaimessa'
    })
  }

  async avaaSuoritusote() {
    const suoritusotePagePromise = this.page.waitForEvent('popup')
    await this.katsoSuoritusoteLink().click()
    return new SuoritusotePage(await suoritusotePagePromise)
  }

  async expectSuostumusPeruttavissa() {
    await expect(this.peruSuostumusLinkki).toBeAttached({
      timeout: 15000
    })
  }

  async expectSuostumusEiPeruttavissa() {
    // Odota, että sivu on (todennäköisesti) latautunut, ennenkuin tarkistetaan, että elementtiä _ei_ näy
    await this.page.waitForTimeout(3000)

    await expect(this.peruSuostumusLinkki).toBeHidden({
      timeout: 15000
    })
  }

  async peruSuostumus() {
    await this.peruSuostumusLinkki.click()

    const confirmBtn = this.page.getByText('Kyllä, peru suostumus')
    await expect(confirmBtn).toBeDisabled()

    await this.page.locator('.SuostumuksenPeruminen .Checkbox__input').click()

    await confirmBtn.click()

    await expect(
      this.page.getByText(
        'Tiedoillasi ei löydy opintosuorituksia eikä opiskeluoikeuksia.'
      )
    ).toBeAttached()
  }
}

type KansalainenUIV2TestIds = typeof KansalainenUIV2TestIds
const KansalainenUIV2TestIds = {
  yoTodistus: {
    language: Select,
    start: Button,
    loading: Label,
    error: Label,
    open: Button
  },
  opiskeluoikeus: KansalainenOpiskeluoikeusHeader()
}
