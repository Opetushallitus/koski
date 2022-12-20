import { DiplomaArviointi } from './DiplomaArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { InternationalSchoolIBOppiaine } from './InternationalSchoolIBOppiaine'

/**
 * DiplomaOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.DiplomaOppiaineenSuoritus`
 */
export type DiplomaOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.DiplomaOppiaineenSuoritus'
  arviointi?: Array<DiplomaArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschooldiplomaoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: InternationalSchoolIBOppiaine
}

export const DiplomaOppiaineenSuoritus = (o: {
  arviointi?: Array<DiplomaArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'internationalschooldiplomaoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: InternationalSchoolIBOppiaine
}): DiplomaOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'internationalschooldiplomaoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.DiplomaOppiaineenSuoritus',
  ...o
})

export const isDiplomaOppiaineenSuoritus = (
  a: any
): a is DiplomaOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.DiplomaOppiaineenSuoritus'
