import { IBExaminationSession } from './IBExaminationSession'
import { IBDiplomanCoreArviointi } from './IBDiplomanCoreArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBDiplomanCoreOppiaine } from './IBDiplomanCoreOppiaine'

/**
 * IBDiplomanCoreSuoritus
 *
 * @see `fi.oph.koski.schema.IBDiplomanCoreSuoritus`
 */
export type IBDiplomanCoreSuoritus = {
  $class: 'fi.oph.koski.schema.IBDiplomanCoreSuoritus'
  examinationSession: IBExaminationSession
  arviointi?: Array<IBDiplomanCoreArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibdiplomancore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: IBDiplomanCoreOppiaine
}

export const IBDiplomanCoreSuoritus = (o: {
  examinationSession: IBExaminationSession
  arviointi?: Array<IBDiplomanCoreArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibdiplomancore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: IBDiplomanCoreOppiaine
}): IBDiplomanCoreSuoritus => ({
  $class: 'fi.oph.koski.schema.IBDiplomanCoreSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibdiplomancore',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

IBDiplomanCoreSuoritus.className =
  'fi.oph.koski.schema.IBDiplomanCoreSuoritus' as const

export const isIBDiplomanCoreSuoritus = (a: any): a is IBDiplomanCoreSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanCoreSuoritus'
