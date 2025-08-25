import { IBCoreOppiaineenArviointi } from './IBCoreOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBDPCoreOppiaine } from './IBDPCoreOppiaine'
import { IBCoreKurssinSuoritus } from './IBCoreKurssinSuoritus'

/**
 * IBDPCoreSuoritus
 *
 * @see `fi.oph.koski.schema.IBDPCoreSuoritus`
 */
export type IBDPCoreSuoritus = {
  $class: 'fi.oph.koski.schema.IBDPCoreSuoritus'
  arviointi?: Array<IBCoreOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibcore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBDPCoreOppiaine
  osasuoritukset?: Array<IBCoreKurssinSuoritus>
}

export const IBDPCoreSuoritus = (o: {
  arviointi?: Array<IBCoreOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibcore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBDPCoreOppiaine
  osasuoritukset?: Array<IBCoreKurssinSuoritus>
}): IBDPCoreSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibcore',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBDPCoreSuoritus',
  ...o
})

IBDPCoreSuoritus.className = 'fi.oph.koski.schema.IBDPCoreSuoritus' as const

export const isIBDPCoreSuoritus = (a: any): a is IBDPCoreSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBDPCoreSuoritus'
