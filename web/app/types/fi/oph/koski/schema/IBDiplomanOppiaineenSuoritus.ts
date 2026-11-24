import { IBExaminationSession } from './IBExaminationSession'
import { IBDiplomanOppiaineenArviointi } from './IBDiplomanOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBDiplomanOppiaine } from './IBDiplomanOppiaine'

/**
 * IBDiplomanOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.IBDiplomanOppiaineenSuoritus`
 */
export type IBDiplomanOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.IBDiplomanOppiaineenSuoritus'
  examinationSession: IBExaminationSession
  arviointi?: Array<IBDiplomanOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibdiplomanoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: IBDiplomanOppiaine
  additionalSubject: boolean
}

export const IBDiplomanOppiaineenSuoritus = (o: {
  examinationSession: IBExaminationSession
  arviointi?: Array<IBDiplomanOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibdiplomanoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: IBDiplomanOppiaine
  additionalSubject: boolean
}): IBDiplomanOppiaineenSuoritus => ({
  $class: 'fi.oph.koski.schema.IBDiplomanOppiaineenSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibdiplomanoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

IBDiplomanOppiaineenSuoritus.className =
  'fi.oph.koski.schema.IBDiplomanOppiaineenSuoritus' as const

export const isIBDiplomanOppiaineenSuoritus = (
  a: any
): a is IBDiplomanOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanOppiaineenSuoritus'
