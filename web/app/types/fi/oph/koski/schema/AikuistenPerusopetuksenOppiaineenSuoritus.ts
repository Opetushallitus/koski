import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AikuistenPerusopetuksenOppiaine } from './AikuistenPerusopetuksenOppiaine'
import { AikuistenPerusopetuksenKurssinSuoritus } from './AikuistenPerusopetuksenKurssinSuoritus'

/**
 * Perusopetuksen oppiaineen suoritus osana aikuisten perusopetuksen oppimäärän suoritusta
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenSuoritus`
 */
export type AikuistenPerusopetuksenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: AikuistenPerusopetuksenOppiaine
  osasuoritukset?: Array<AikuistenPerusopetuksenKurssinSuoritus>
}

export const AikuistenPerusopetuksenOppiaineenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'aikuistenperusopetuksenoppiaine'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  koulutusmoduuli: AikuistenPerusopetuksenOppiaine
  osasuoritukset?: Array<AikuistenPerusopetuksenKurssinSuoritus>
}): AikuistenPerusopetuksenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'aikuistenperusopetuksenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenSuoritus',
  ...o
})

export const isAikuistenPerusopetuksenOppiaineenSuoritus = (
  a: any
): a is AikuistenPerusopetuksenOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AikuistenPerusopetuksenOppiaineenSuoritus'
