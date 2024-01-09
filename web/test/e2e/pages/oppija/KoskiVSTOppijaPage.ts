import { Page } from '@playwright/test'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { BuiltIdNode, arrayOf, composedField } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { FormField } from './uiV2builder/controls'
import { Input } from './uiV2builder/Input'
import { Label } from './uiV2builder/Label'
import { OpiskeluoikeusHeader } from './uiV2builder/OpiskeluoikeusHeader'
import { Select } from './uiV2builder/Select'
import {
  SuorituksenVahvistus,
  isMerkitseKeskeneräiseksiDisabled,
  isMerkitseValmiiksiDisabled,
  vahvistaSuoritusTallennetullaHenkilöllä,
  vahvistaSuoritusUudellaHenkilöllä
} from './uiV2builder/SuorituksenVahvistus'

export class KoskiVSTOppijaPage extends KoskiOppijaPageV2<
  typeof VapaanSivistystyönTestIds
> {
  constructor(page: Page) {
    super(page, VapaanSivistystyönTestIds)
  }

  async setPeruste(diaarinumero: string) {
    await this.$.suoritukset(0).peruste.set(diaarinumero)
  }

  async peruste() {
    return this.$.suoritukset(0).peruste.value(this.editMode)
  }

  osasuoritus(index: number) {
    return new VSTOsasuoritus(
      this.page,
      this.$.suoritukset(0).osasuoritukset(index),
      this.editMode
    )
  }

  async osasuoritusOptions() {
    return await this.$.suoritukset(0).addOsasuoritus.select.options()
  }

  async addOsasuoritus(arvo: string) {
    await this.$.suoritukset(0).addOsasuoritus.select.set(arvo)
  }

  async addNewOsasuoritus(nimi: string) {
    const element = this.$.suoritukset(0).addOsasuoritus
    await element.select.set('__NEW__')
    await element.modal.nimi.set(nimi)
    await element.modal.submit.click()
    await this.page.waitForLoadState('networkidle')
  }

  async removeOsasuoritus(index: number) {
    await this.$.suoritukset(0).osasuoritukset(index).delete.click()
  }

  async suorituksenLaajuus() {
    return this.$.suoritukset(0).laajuus.value()
  }

  async laajuudetYhteensä() {
    return this.$.suoritukset(0).yhteensa.value()
  }

  async vahvistaSuoritusUudellaHenkilöllä(
    nimi: string,
    titteli: string,
    pvm: string
  ) {
    await vahvistaSuoritusUudellaHenkilöllä(
      this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus,
      nimi,
      titteli,
      pvm
    )
  }

  async vahvistaSuoritusTallennetullaHenkilöllä(nimi: string, pvm: string) {
    await vahvistaSuoritusTallennetullaHenkilöllä(
      this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus,
      nimi,
      pvm
    )
  }

  async isMerkitseKeskeneräiseksiDisabled() {
    return isMerkitseKeskeneräiseksiDisabled(
      this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus
    )
  }

  async isMerkitseValmiiksiDisabled() {
    return isMerkitseValmiiksiDisabled(
      this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus
    )
  }

  async mitätöi() {
    await this.$.opiskeluoikeus.invalidate.button.click()
    await this.$.opiskeluoikeus.invalidate.confirm.click()
  }
}

type VSTOsasuoritusSchema = BuiltIdNode<
  ReturnType<typeof VapaanSivistystyönOsasuoritusTestIds>
>
export class VSTOsasuoritus {
  page: Page
  editMode: boolean
  osasuoritus: VSTOsasuoritusSchema

  constructor(
    page: Page,
    osasuoritus: VSTOsasuoritusSchema,
    editMode: boolean
  ) {
    this.page = page
    this.editMode = editMode
    this.osasuoritus = osasuoritus
  }

  async nimi() {
    return this.osasuoritus.nimi.value(this.editMode)
  }

  async laajuus() {
    return this.osasuoritus.laajuus.value(this.editMode)
  }

  async arvosana() {
    return this.osasuoritus.arvosana.value(this.editMode)
  }

  async taitotaso() {
    return this.osasuoritus.taitotaso.value(this.editMode)
  }

  async isExpanded() {
    return (await this.osasuoritus.expand.value()) === ''
  }

  async expand() {
    if (!(await this.isExpanded())) {
      return this.osasuoritus.expand.click()
    }
  }

  async delete() {
    return this.osasuoritus.delete.click()
  }

  async setLaajuus(laajuus: number) {
    return this.osasuoritus.laajuus.set(laajuus.toString())
  }

  async setArvosana(koodisto: string, koodi: string) {
    return this.osasuoritus.arvosana.set(`${koodisto}_${koodi}`)
  }

  async setVapaatavoitteinenArvosana(arvosana: number) {
    return this.setArvosana(
      'Arviointiasteikko VST vapaatavoitteinen.arviointiasteikkovstvapaatavoitteinen',
      arvosana.toString()
    )
  }

  async setJotpaArvosana(arvosana: number) {
    return this.setArvosana(
      'Arviointiasteikko VST JOTPA.arviointiasteikkovstjotpa',
      arvosana.toString()
    )
  }

  async setArvostelunPvm(pvm: string) {
    return this.osasuoritus.properties.arviointi(0).date.set(pvm)
  }

  async arvostelunPvm() {
    return this.osasuoritus.properties.arviointi(0).date.value(this.editMode)
  }

  async setKuvaus(kuvaus: string) {
    return this.osasuoritus.properties.kuvaus.set(kuvaus)
  }

  async kuvaus() {
    return this.osasuoritus.properties.kuvaus.value(this.editMode)
  }

  async addAlaosasuoritus(koodi: string) {
    return this.osasuoritus.properties.addOsasuoritus.select.set(koodi)
  }

  async addNewAlaosasuoritus(nimi: string, optionId = '__NEW__') {
    const addNew = this.osasuoritus.properties.addOsasuoritus
    await addNew.select.set(optionId)
    await addNew.modal.nimi.set(nimi)
    await addNew.modal.submit.click()
    await this.page.waitForLoadState('networkidle')
  }

  async deleteAlaosasuoritus(index: number) {
    return this.osasuoritus.properties.osasuoritukset(index).delete.click()
  }

  async alaosasuoritusOptions() {
    return this.osasuoritus.properties.addOsasuoritus.select.options()
  }

  async setSuoritusarvosana(hyväksytty: boolean, grouped = false) {
    return this.osasuoritus.arvosana.set(
      `${grouped ? 'Arviointiasteikko VST.' : ''}arviointiasteikkovst_${
        hyväksytty ? 'Hyväksytty' : 'Hylätty'
      }`
    )
  }

  async setKielenTaitotaso(taitotaso: string) {
    return this.osasuoritus.taitotaso.set(
      `arviointiasteikkokehittyvankielitaidontasot_${taitotaso}`
    )
  }

  alaosasuoritus(index: number) {
    return new VSTOsasuoritus(
      this.page,
      this.osasuoritus.properties.osasuoritukset(index),
      this.editMode
    )
  }
}

const AddOsasuoritusField = {
  select: Select,
  modal: {
    nimi: FormField(Input, Input),
    cancel: Button,
    submit: Button
  }
}

// Kääräisty taulukkoa mallintavaksi rakenteeksi, jotta se pystyy viittaamaan itseensä
const VapaanSivistystyönOsasuoritusTestIds = (_index: number) => ({
  expand: Button,
  arvosana: FormField(Label, Select),
  taitotaso: FormField(Label, Select),
  laajuus: FormField(Label, Input),
  nimi: FormField(Label),
  properties: {
    arviointi: arrayOf({
      date: FormField(Label, Input),
      arvosana: FormField(Label, Input)
    }),
    tunnustettu: FormField(Label),
    kuvaus: FormField(Label, Input),

    osasuoritukset: VapaanSivistystyönOsasuoritusTestIds,
    addOsasuoritus: {
      ...AddOsasuoritusField,
      paikallinen: AddOsasuoritusField
    }
  },

  delete: Button
})

const VapaanSivistystyönTestIds = {
  opiskeluoikeus: OpiskeluoikeusHeader(),
  suoritusTabs: arrayOf({ tab: Button }),
  suoritukset: arrayOf({
    tab: Button,
    oppilaitos: FormField(Label),
    koulutus: FormField(Label),
    opetuskieli: FormField(Label),
    lisatiedot: FormField(Label),
    peruste: FormField(Label, Select),
    laajuus: FormField(Label),

    suorituksenVahvistus: SuorituksenVahvistus(),
    expand: Button,
    osasuoritukset: VapaanSivistystyönOsasuoritusTestIds,
    addOsasuoritus: AddOsasuoritusField,

    yhteensa: Label
  })
}
