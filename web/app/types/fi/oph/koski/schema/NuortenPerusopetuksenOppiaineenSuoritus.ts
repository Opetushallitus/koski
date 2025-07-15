import { PerusopetuksenOppiaineenArviointi } from './PerusopetuksenOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { NuortenPerusopetuksenOppiaine } from './NuortenPerusopetuksenOppiaine'

/**
 * Perusopetuksen oppiaineen suoritus osana perusopetuksen oppimäärän tai vuosiluokan suoritusta
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritus`
 */
export type NuortenPerusopetuksenOppiaineenSuoritus = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritus'
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  rajattuOppimäärä: boolean
  painotettuOpetus: boolean
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
  yksilöllistettyOppimäärä: boolean
}

export const NuortenPerusopetuksenOppiaineenSuoritus = (o: {
  arviointi?: Array<PerusopetuksenOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'perusopetuksenoppiaine'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  luokkaAste?: Koodistokoodiviite<'perusopetuksenluokkaaste', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'perusopetuksensuoritustapa',
    'erityinentutkinto'
  >
  rajattuOppimäärä?: boolean
  painotettuOpetus: boolean
  koulutusmoduuli: NuortenPerusopetuksenOppiaine
  yksilöllistettyOppimäärä?: boolean
}): NuortenPerusopetuksenOppiaineenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'perusopetuksenoppiaine',
    koodistoUri: 'suorituksentyyppi'
  }),
  rajattuOppimäärä: false,
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritus',
  yksilöllistettyOppimäärä: false,
  ...o
})

NuortenPerusopetuksenOppiaineenSuoritus.className =
  'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritus' as const

export const isNuortenPerusopetuksenOppiaineenSuoritus = (
  a: any
): a is NuortenPerusopetuksenOppiaineenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.NuortenPerusopetuksenOppiaineenSuoritus'
