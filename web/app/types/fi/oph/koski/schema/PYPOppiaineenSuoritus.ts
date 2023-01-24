import { SanallinenInternationalSchoolOppiaineenArviointi } from './SanallinenInternationalSchoolOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { PYPOppiaine } from './PYPOppiaine'

/**
 * PYPOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.PYPOppiaineenSuoritus`
 */
export type PYPOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.PYPOppiaineenSuoritus'
  arviointi?: Array<SanallinenInternationalSchoolOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolpypoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PYPOppiaine
}

export const PYPOppiaineenSuoritus = (o: {
  arviointi?: Array<SanallinenInternationalSchoolOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschoolpypoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: PYPOppiaine
}): PYPOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschoolpypoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.PYPOppiaineenSuoritus',
  ...o
})

PYPOppiaineenSuoritus.className =
  'fi.oph.koski.schema.PYPOppiaineenSuoritus' as const

export const isPYPOppiaineenSuoritus = (a: any): a is PYPOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.PYPOppiaineenSuoritus'
