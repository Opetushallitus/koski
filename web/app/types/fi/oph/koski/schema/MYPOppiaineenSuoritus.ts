import { MYPArviointi } from './MYPArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MYPOppiaine } from './MYPOppiaine'

/**
 * MYPOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.MYPOppiaineenSuoritus`
 */
export type MYPOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.MYPOppiaineenSuoritus'
  arviointi?: Array<MYPArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolmypoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MYPOppiaine
}

export const MYPOppiaineenSuoritus = (o: {
  arviointi?: Array<MYPArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolmypoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MYPOppiaine
}): MYPOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschoolmypoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MYPOppiaineenSuoritus',
  ...o
})

MYPOppiaineenSuoritus.className =
  'fi.oph.koski.schema.MYPOppiaineenSuoritus' as const

export const isMYPOppiaineenSuoritus = (a: any): a is MYPOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.MYPOppiaineenSuoritus'
