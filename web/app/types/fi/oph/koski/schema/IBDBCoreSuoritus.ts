import { IBOppiaineenArviointi } from './IBOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBDPCoreOppiaine } from './IBDPCoreOppiaine'
import { IBKurssinSuoritus } from './IBKurssinSuoritus'

/**
 * IBDBCoreSuoritus
 *
 * @see `fi.oph.koski.schema.IBDBCoreSuoritus`
 */
export type IBDBCoreSuoritus = {
  $class: 'fi.oph.koski.schema.IBDBCoreSuoritus'
  arviointi?: Array<IBOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibcore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBDPCoreOppiaine
  osasuoritukset?: Array<IBKurssinSuoritus>
}

export const IBDBCoreSuoritus = (o: {
  arviointi?: Array<IBOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibcore'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBDPCoreOppiaine
  osasuoritukset?: Array<IBKurssinSuoritus>
}): IBDBCoreSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibcore',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBDBCoreSuoritus',
  ...o
})

IBDBCoreSuoritus.className = 'fi.oph.koski.schema.IBDBCoreSuoritus' as const

export const isIBDBCoreSuoritus = (a: any): a is IBDBCoreSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBDBCoreSuoritus'
