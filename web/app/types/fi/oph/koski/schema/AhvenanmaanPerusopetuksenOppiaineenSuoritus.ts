import { AhvenanmaanPerusopetuksenOppiaineenArviointi } from './AhvenanmaanPerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { AhvenanmaanPerusopetuksenOppiaine } from './AhvenanmaanPerusopetuksenOppiaine'

/**
 * Ahvenanmaan perusopetuksen oppiaineen suoritus osana oppimäärän tai vuosiluokan suoritusta.
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaineenSuoritus`
 */
export type AhvenanmaanPerusopetuksenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaineenSuoritus'
  arviointi?: Array<AhvenanmaanPerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenoppiaine'
  >
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  mukautettuOppimäärä: boolean
  koulutusmoduuli: AhvenanmaanPerusopetuksenOppiaine
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
}

export const AhvenanmaanPerusopetuksenOppiaineenSuoritus = (o: {
  arviointi?: Array<AhvenanmaanPerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ahvenanmaanperusopetuksenoppiaine'
  >
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  mukautettuOppimäärä?: boolean
  koulutusmoduuli: AhvenanmaanPerusopetuksenOppiaine
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
}): AhvenanmaanPerusopetuksenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ahvenanmaanperusopetuksenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  mukautettuOppimäärä: false,
  $class: 'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaineenSuoritus',
  ...o
})

AhvenanmaanPerusopetuksenOppiaineenSuoritus.className =
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaineenSuoritus' as const

export const isAhvenanmaanPerusopetuksenOppiaineenSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenOppiaineenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaineenSuoritus'
