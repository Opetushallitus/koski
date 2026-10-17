import { Page } from '@playwright/test'
import { sum } from '../../../../app/util/numbers'
import { expect } from '../../base'
import { KoskiOppijaPageV2 } from './KoskiOppijaPageV2'
import { arrayOf } from './uiV2builder/builder'
import { Button } from './uiV2builder/Button'
import { Checkbox } from './uiV2builder/Checkbox'
import { FormField } from './uiV2builder/controls'
import { Input } from './uiV2builder/Input'
import { LaajuusInput } from './uiV2builder/LaajuusInput'
import { Label } from './uiV2builder/Label'
import { OpiskeluoikeusHeader } from './uiV2builder/OpiskeluoikeusHeader'
import { Select } from './uiV2builder/Select'
import { SuorituksenVahvistus } from './uiV2builder/SuorituksenVahvistus'

export class KoskiIBOppijaPage extends KoskiOppijaPageV2<typeof IBTestIds> {
  constructor(page: Page) {
    super(page, IBTestIds)
  }

  async testOppiaineryhmät(...odotetutTiedot: OppiaineryhmätExpectedData[]) {
    await this.testSuorituksenOppiaineryhmät(0, ...odotetutTiedot)
  }

  async testSuorituksenOppiaineryhmät(
    suoritusIndex: number,
    ...odotetutTiedot: OppiaineryhmätExpectedData[]
  ) {
    for (const [aineryhmäIndex, aineryhmä] of odotetutTiedot.entries()) {
      const { aineryhmä: aineryhmänNimi, oppiaineet } = aineryhmä
      const oppiaineryhmä = this.oppiaineryhmä(suoritusIndex, aineryhmäIndex)

      if (aineryhmänNimi) {
        await expect(oppiaineryhmä.nimi.elem).toHaveText(aineryhmänNimi)
      }

      for (const [oppiaineIndex, oppiaineTiedot] of oppiaineet.entries()) {
        const { nimi, arvosana, predictedGrade, kurssit } = oppiaineTiedot
        const laajuus = sum(kurssit.map((kurssi) => kurssi.laajuus || 1))
        const oppiaine = oppiaineryhmä.oppiaineet(oppiaineIndex)

        await expect(oppiaine.nimi.elem).toHaveText(nimi)
        if (arvosana) {
          await expect(oppiaine.arvosana.viewer).toHaveText(arvosana)
        }
        if (predictedGrade) {
          await expect(oppiaine.predictedGrade.viewer).toHaveText(
            predictedGrade
          )
        }
        await expect(oppiaine.laajuus.elem).toHaveText(laajuus.toString())

        for (const [kurssiIndex, kurssinTiedot] of kurssit.entries()) {
          const {
            tunniste,
            arvosana: kurssinArvosana,
            paikallinen
          } = kurssinTiedot
          const kurssi = oppiaine.kurssit(kurssiIndex)

          await expect(kurssi.tunniste.button).toHaveText(
            paikallinen ? `${tunniste} *` : tunniste
          )
          await expect(kurssi.arvosana.viewer).toHaveText(kurssinArvosana)
        }
        const olematonKurssi = oppiaine.kurssit(kurssit.length)
        await expect(olematonKurssi.tunniste.button).not.toBeAttached()
      }
    }
  }

  oppiaineryhmä(suoritusIndex: number = 0, ryhmäIndex: number = 0) {
    return this.$.suoritukset(suoritusIndex).oppiaineryhmät(ryhmäIndex)
  }
}

const PaikallinenKoulutusFields = {
  nimi: Input,
  koodiarvo: Input,
  kuvaus: Input
}

const KurssiFields = {
  tunniste: Button,
  arvosana: FormField(Label, Select),
  delete: Button,
  modal: {
    laajuus: LaajuusInput
  }
}

export const IBTestIds = {
  opiskeluoikeus: OpiskeluoikeusHeader(),
  suoritusTabs: arrayOf({ tab: Button }),
  suoritukset: arrayOf({
    koulutus: Label,
    organisaatio: FormField(Label, Select),
    ryhmä: FormField(Label, Input),
    suorituskieli: FormField(Label, Select),
    todistuksellaNäkyvätLisätiedot: FormField(Label, Input),

    theoryOfKnowledge: {
      arvosana: FormField(Label, Select),
      pakollinen: FormField(Label, Select),
      kurssit: arrayOf(KurssiFields)
    },
    extendedEssay: {
      oppiaine: Label,
      kieli: Label,
      taso: Label,
      ryhma: Label,
      pakollinen: Label,
      aihe: Label,
      arvosana: Label
    },
    creativityActionService: Label,
    lisäpisteet: FormField(Label, Select),

    suorituksenVahvistus: SuorituksenVahvistus(),

    oppiaineryhmät: arrayOf({
      nimi: Label,
      oppiaineet: arrayOf({
        nimi: Label,
        laajuus: Label,
        arvosana: FormField(Label, Select),
        predictedGrade: FormField(Label, Select),
        kurssit: arrayOf(KurssiFields),
        // Uuden kurssin lisäys:
        addKurssi: Button,
        modal: {
          tunniste: Select,
          tyyppi: Select,
          kieli: Select,
          suorituskieli: Select,
          laajuus: FormField(Label, Input),
          paikallinenKoulutus: PaikallinenKoulutusFields,
          pakollinen: Checkbox,
          cancel: Button,
          submit: Button
        },
        // Oppiaineen poisto
        delete: Button
      })
    }),
    suoritettujaKurssejaYhteensä: Label,

    // Oppiaineen lisäys
    addOppiaine: Button,
    modal: {
      tunniste: Select,
      aineryhmä: Select,
      kieli: Select,
      matematiikanOppimäärä: Select,
      äidinkielenKieli: Select,
      taso: Select,
      paikallinenKoulutus: PaikallinenKoulutusFields,
      cancel: Button,
      submit: Button
    }
  })
}

export type OppiaineryhmätExpectedData = {
  aineryhmä?: string
  oppiaineet: Array<{
    nimi: string
    arvosana?: string
    predictedGrade?: string
    kurssit: Array<{
      tunniste: string
      paikallinen?: boolean
      arvosana: string
      laajuus?: number
    }>
  }>
}
