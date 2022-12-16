import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetuksenAlkuvaiheenOppiaine } from './AikuistenPerusopetuksenAlkuvaiheenOppiaine'
import { AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus } from './AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus'

/**
 * Oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän alkuvaiheen suoritusta
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus`
 */
export type AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenalkuvaiheenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenOppiaine
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus>
}

export const AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenalkuvaiheenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: AikuistenPerusopetuksenAlkuvaiheenOppiaine
  osasuoritukset?: Array<AikuistenPerusopetuksenAlkuvaiheenKurssinSuoritus>
}): AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenalkuvaiheenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus',
  ...o
})

export const isAikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus =>
  a?.$class === 'AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'
