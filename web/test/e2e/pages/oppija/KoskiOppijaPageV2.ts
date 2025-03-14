import { Locator, Page, expect } from '@playwright/test'
import { build, BuiltIdNode, IdNodeObject } from './uiV2builder/builder'
import { notUndefined } from '../../../../app/util/util'

export class KoskiOppijaPageV2<T extends IdNodeObject<string>> {
  page: Page
  $: BuiltIdNode<T>
  private idHierarchy: T
  editMode: boolean
  suoritusIndex: number
  osasuoritusIndex: number

  saveBtn: Locator
  saveSnackbar: Locator
  errors: Locator

  constructor(page: Page, idHierarchy: T) {
    this.page = page
    this.idHierarchy = idHierarchy
    this.$ = this.opiskeluoikeus(0)
    this.suoritusIndex = 0
    this.osasuoritusIndex = 0
    this.editMode = false

    this.saveBtn = this.getByTestId('opiskeluoikeus.save')
    this.saveSnackbar = this.getByTestId('opiskeluoikeus.saved.snackbar')
    this.errors = page.getByTestId('globalErrors')
  }

  getByTestId(id?: string, ooIndex?: number) {
    return this.page.getByTestId([`oo.${ooIndex || 0}`, id].filter(notUndefined).join('.'))
  }

  opiskeluoikeus(index: number) {
    return build(this.page, this.idHierarchy, `oo.${index}`)
  }

  async goto(oppijaOid: string) {
    await this.page.goto(`/koski/oppija/${oppijaOid}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)

    this.suoritusIndex = 0
    this.osasuoritusIndex = 0
    this.editMode = false
  }

  async gotoWithQueryParams(oid: string, queryParams: Record<string, string>) {
    const params = new URLSearchParams(queryParams)
    await this.page.goto(`/koski/oppija/${oid}?${params.toString()}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)
  }

  async selectOpiskeluoikeus(tyyppi: string) {
    const opiskeluoikeusTab = this.page.getByTestId(
      `opiskeluoikeustyyppi-${tyyppi}`
    )
    await opiskeluoikeusTab.click()
    await expect(opiskeluoikeusTab).toHaveAttribute('data-selected', 'true')
  }

  async edit() {
    this.editMode = true
    return this.$.opiskeluoikeus.edit.click()
  }

  async cancelEdit() {
    this.editMode = false
    return this.$.opiskeluoikeus.cancelEdit.click()
  }

  async selectSuoritus(index: number) {
    await this.$.suoritusTabs(index).tab.click()
    this.suoritusIndex = index
    this.osasuoritusIndex = 0
  }

  async openPäätasonOsasuoritus() {
    await this.$.suoritukset(this.suoritusIndex).expand.click()
  }

  async openOsasuoritus(index: number) {
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(index)
      .expand.click()
    this.osasuoritusIndex = index
  }

  async opiskeluoikeudenTila(index: number) {
    const item =
      this.$.opiskeluoikeus.tila[this.editMode ? 'edit' : 'value'].items(index)
    return `${await item.date.value(this.editMode)} ${await item.tila.value()}`
  }

  async removeOpiskeluoikeudenTila(index: number) {
    await this.$.opiskeluoikeus.tila.edit.items(index).remove.click()
  }

  async suorituksenTila() {
    return this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus[
      this.editMode ? 'edit' : 'value'
    ].status.value()
  }

  async suorituksenVahvistus() {
    return this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus[
      this.editMode ? 'edit' : 'value'
    ].details.value()
  }

  async suorituksenVahvistushenkilö(index: number) {
    return this.$.suoritukset(this.suoritusIndex)
      .suorituksenVahvistus[this.editMode ? 'edit' : 'value'].henkilö(index)
      .value()
  }

  async poistaSuorituksenVahvistus() {
    await this.$.suoritukset(
      this.suoritusIndex
    ).suorituksenVahvistus.edit.merkitseKeskeneräiseksi.click()
  }

  async tallenna() {
    await this.saveBtn.click()
    await this.page.waitForLoadState('networkidle')
    await this.saveSnackbar.waitFor({ state: 'visible', timeout: 3000 })
    expect(await this.errors.isVisible()).toBeFalsy()
    this.editMode = false
  }

  async tallennaVirheellisenä(...errors: string[]) {
    await this.saveBtn.click()
    await this.page.waitForLoadState('networkidle')
    expect(await this.errors.textContent()).toEqual(errors.join(''))
  }
}
