import { Page } from '@playwright/test'
import { TaiteenPerusopetuksenArviointi } from '../../../../app/types/fi/oph/koski/schema/TaiteenPerusopetuksenArviointi'
import { KoodiviiteIdOf } from '../../../../app/util/koodisto'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { arrayOf } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { FormField } from './uiV2builder/controls'
import { Input } from './uiV2builder/Input'
import { Label } from './uiV2builder/Label'
import { OpiskeluoikeusHeader } from './uiV2builder/OpiskeluoikeusHeader'
import { Select } from './uiV2builder/Select'
import {
  SuorituksenVahvistus,
  vahvistaSuoritusTallennetullaHenkilöllä,
  vahvistaSuoritusUudellaHenkilöllä
} from './uiV2builder/SuorituksenVahvistus'
import { Tunnustettu } from './uiV2builder/Tunnustettu'

export class KoskiTpoOppijaPage extends KoskiOppijaPageV2<
  typeof TaiteenPerusopetusTestIds
> {
  constructor(page: Page) {
    super(page, TaiteenPerusopetusTestIds)
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

  async osasuoritustieto(key: 'nimi' | 'laajuus' | 'arvosana') {
    const oo = this.$.suoritukset(this.suoritusIndex).osasuoritukset(
      this.osasuoritusIndex
    )[key]
    return oo.value(this.editMode)
  }

  async osasuoritusProperty(key: 'arvosana' | 'date' | 'tunnustettu') {
    return this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      .properties[key].value(this.editMode)
  }

  async addNewOsasuoritus(nimi: string) {
    const element = this.$.suoritukset(this.suoritusIndex).addOsasuoritus
    await element.select.set('__NEW__')
    await element.modal.nimi.set(nimi)
    await element.modal.submit.click()
    await this.page.waitForLoadState('networkidle')
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

  async setTunnustettu(selite: string) {
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      .expand.click()
    await this.$.suoritukset(this.suoritusIndex)
      .osasuoritukset(this.osasuoritusIndex)
      .properties.tunnustettu.set(selite)
  }

  async addOpiskeluoikeudenTila(pvm: string, tila: string) {
    const editor = this.$.opiskeluoikeus.tila.edit
    await editor.add.click()
    await editor.modal.date.set(pvm)
    await editor.modal.tila.set(tila)
    await editor.modal.submit.click()
  }

  async vahvistaSuoritusUudellaHenkilöllä(
    nimi: string,
    pvm: string,
    titteli?: string
  ) {
    await vahvistaSuoritusUudellaHenkilöllä(
      this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus,
      nimi,
      pvm,
      titteli
    )
  }

  async vahvistaSuoritusTallennetullaHenkilöllä(nimi: string, pvm: string) {
    await vahvistaSuoritusTallennetullaHenkilöllä(
      this.$.suoritukset(this.suoritusIndex).suorituksenVahvistus,
      nimi,
      pvm
    )
  }
}

const TaiteenPerusopetusTestIds = {
  opiskeluoikeus: OpiskeluoikeusHeader(),
  suoritusTabs: arrayOf({ tab: Button }),
  suoritukset: arrayOf({
    taiteenala: FormField(Label),
    oppimäärä: FormField(Label),
    koulutuksenToteutustapa: FormField(Label),
    oppilaitos: FormField(Label),
    koulutustoimija: FormField(Label),
    laajuus: FormField(Label),

    suorituksenVahvistus: SuorituksenVahvistus(),
    expand: Button,

    osasuoritukset: arrayOf({
      expand: Button,
      arvosana: FormField(Label, Select),
      laajuus: FormField(Label, Input),
      nimi: FormField(Label),
      properties: {
        arvosana: FormField(Label),
        date: FormField(Label),
        tunnustettu: FormField(Label, Tunnustettu)
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
    }
  })
}
