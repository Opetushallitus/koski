import { Page } from '@playwright/test'
import { TaiteenPerusopetuksenArviointi } from '../../../../app/types/fi/oph/koski/schema/TaiteenPerusopetuksenArviointi'
import { KoodiviiteIdOf } from '../../../../app/util/koodisto'
import { expect } from '../../base'
import { arrayOf, build, BuiltIdNode } from './uiV2builder/builder'
import {
  Button,
  FormField,
  Input,
  NumberInput,
  RadioButtons,
  Select,
  Text
} from './uiV2builder/controls'

export class KoskiTpoOppijaPage {
  page: Page
  $: BuiltIdNode<typeof TaiteenPerusopetusTestIds>
  editMode: boolean
  suoritusIndex: number
  osasuoritusIndex: number

  constructor(page: Page) {
    this.page = page
    this.$ = build(page, TaiteenPerusopetusTestIds)
    this.suoritusIndex = 0
    this.osasuoritusIndex = 0
    this.editMode = false
  }

  async goto(oppijaOid: string) {
    await this.page.goto(`/koski/oppija/${oppijaOid}`)
    await expect(this.page).toHaveURL(/\/koski\/oppija\/1\.2\..*/)

    this.suoritusIndex = 0
    this.osasuoritusIndex = 0
    this.editMode = false
  }

  async edit() {
    this.editMode = true
    return this.$.opiskeluoikeus.edit.click()
  }

  async selectSuoritus(index: number) {
    await this.$.suoritukset(index).tab.click()
    this.suoritusIndex = index
    this.osasuoritusIndex = 0
  }

  async suoritustieto(
    key:
      | 'koulutuksenToteutustapa'
      | 'laajuus'
      | 'oppilaitos'
      | 'oppimäärä'
      | 'taiteenala'
  ) {
    return this.$.suoritukset(this.suoritusIndex)[key].value(this.editMode)
  }

  async openOsasuoritus(index: number) {
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(index)
      .expand.click()
    this.osasuoritusIndex = index
  }

  async osasuoritustieto(key: 'nimi' | 'laajuus' | 'arvosana') {
    return this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      [key].value(this.editMode)
  }

  async osasuoritusProperty(key: 'arvosana' | 'arvostelunPvm' | 'tunnustettu') {
    return this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      .properties[key].value(this.editMode)
  }

  async addNewOsasuoritus(nimi: string) {
    const element = this.$.suoritukset(this.suoritusIndex).addOsasuoritus
    await element.select.set('__NEW__')
    await element.modal.nimi.set(nimi)
    await element.modal.submit.click()
  }

  async addOsasuoritus(koodiarvo: string) {
    const element = this.$.suoritukset(this.suoritusIndex).addOsasuoritus
    await element.select.set(koodiarvo)
  }

  async removeOsasuoritus(index: number) {
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(index)
      .delete.click()
  }

  async deleteStoredOsasuoritus(key: string) {
    await this.$.suoritukset(this.suoritusIndex).addOsasuoritus.select.delete(
      key
    )
  }

  async setOsasuorituksenLaajuus(opintopisteet: number) {
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      .laajuus.set(opintopisteet.toString())
  }

  async setOsasuorituksenArvosana(
    arvosana: KoodiviiteIdOf<TaiteenPerusopetuksenArviointi['arvosana']>
  ) {
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      .arvosana.set(arvosana)
  }

  async opiskeluoikeudenTila(index: number) {
    const item =
      this.$.opiskeluoikeus.tila[this.editMode ? 'edit' : 'value'].items(index)
    return `${await item.date.value()} ${await item.tila.value()}`
  }

  async addOpiskeluoikeudenTila(pvm: string, tila: string) {
    const editor = this.$.opiskeluoikeus.tila.edit
    await editor.add.click()
    await editor.modal.date.set(pvm)
    await editor.modal.tila.set(tila)
    await editor.modal.submit.click()
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

  async vahvistaSuoritusUudellaHenkilöllä(
    nimi: string,
    titteli: string,
    pvm: string
  ) {
    const vahvistus = this.$.suoritukset(this.suoritusIndex)
      .suorituksenVahvistus.edit

    await vahvistus.merkitseValmiiksi.click()

    await vahvistus.modal.date.set(pvm)

    const myöntäjät = vahvistus.modal.myöntäjät.edit
    await myöntäjät.add.set('__NEW__')

    const henkilö = myöntäjät.newHenkilö(0)
    await henkilö.nimi.set(nimi)
    await henkilö.titteli.set(titteli)

    await vahvistus.modal.submit.click()
  }

  async vahvistaSuoritusTallennetullaHenkilöllä(nimi: string, pvm: string) {
    const vahvistus = this.$.suoritukset(this.suoritusIndex)
      .suorituksenVahvistus.edit

    await vahvistus.merkitseValmiiksi.click()
    await vahvistus.modal.date.set(pvm)
    await vahvistus.modal.myöntäjät.edit.add.set(nimi)
    await vahvistus.modal.submit.click()
  }

  async poistaSuorituksenVahvistus() {
    await this.$.suoritukset(
      this.suoritusIndex
    ).suorituksenVahvistus.edit.merkitseKeskeneräiseksi.click()
  }
}

const TaiteenPerusopetusTestIds = {
  opiskeluoikeus: {
    nimi: Text,
    oid: Text,
    voimassaoloaika: Text,
    edit: Button,
    tila: {
      value: {
        items: arrayOf({
          date: Text,
          tila: Text
        })
      },
      edit: {
        items: arrayOf({
          date: Input,
          tila: Text,
          remove: Button
        }),
        add: Button,
        modal: {
          date: FormField(Input, Input),
          tila: FormField(RadioButtons, RadioButtons),
          submit: Button,
          cancel: Button
        }
      }
    }
  },
  suoritukset: arrayOf({
    tab: Button,
    koulutuksenToteutustapa: FormField(Text),
    laajuus: FormField(Text),
    oppilaitos: FormField(Text),
    oppimäärä: FormField(Text),
    taiteenala: FormField(Text),
    osasuoritukset: arrayOf({
      expand: Button,
      arvosana: FormField(Text, Select),
      laajuus: FormField(Text, NumberInput),
      nimi: FormField(Text),
      properties: {
        arvosana: FormField(Text),
        arvostelunPvm: FormField(Text),
        tunnustettu: FormField(Text)
      },
      delete: Button
    }),
    addOsasuoritus: {
      select: Select,
      modal: {
        nimi: FormField(Input, Input),
        cancel: Button,
        submit: Button
      }
    },
    suorituksenVahvistus: {
      value: {
        status: Text,
        details: Text,
        henkilö: arrayOf(Text)
      },
      edit: {
        status: Text,
        details: Text,
        henkilö: arrayOf(Text),
        merkitseValmiiksi: Button,
        merkitseKeskeneräiseksi: Button,
        modal: {
          date: FormField(Input, Input),
          myöntäjät: {
            edit: {
              add: Select,
              henkilö: arrayOf({ delete: Button }),
              newHenkilö: arrayOf({ nimi: Input, titteli: Input }),
              storedHenkilö: arrayOf(Select)
            }
          },
          organisaatio: FormField(Select, Select),
          paikkakunta: FormField(Select, Select),
          submit: Button,
          cancel: Button
        }
      }
    }
  })
}
