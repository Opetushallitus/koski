import { IBDiplomanCoreArviointi } from './IBDiplomanCoreArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBTutkintokerta } from './IBTutkintokerta'
import { IBDiplomanCoreOppiaine } from './IBDiplomanCoreOppiaine'

/**
 * Extended essayn (EE) tai Theory of knowledgen (TOK) tulos
 *
 * @see `fi.oph.koski.schema.IBDiplomanCoreSuoritus`
 */
export type IBDiplomanCoreSuoritus = {
  $class: 'fi.oph.koski.schema.IBDiplomanCoreSuoritus'
  arviointi?: Array<IBDiplomanCoreArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibdiplomancore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: IBTutkintokerta
  koulutusmoduuli: IBDiplomanCoreOppiaine
}

export const IBDiplomanCoreSuoritus = (o: {
  arviointi?: Array<IBDiplomanCoreArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibdiplomancore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  tutkintokerta: IBTutkintokerta
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
