import { IBCASOppiaineenArviointi } from './IBCASOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBOppiaineCAS } from './IBOppiaineCAS'

/**
 * CAS-suorituksen tiedot
 *
 * @see `fi.oph.koski.schema.IBCASSuoritus`
 */
export type IBCASSuoritus = {
  $class: 'fi.oph.koski.schema.IBCASSuoritus'
  arviointi?: Array<IBCASOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainecas'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineCAS
}

export const IBCASSuoritus = (o: {
  arviointi?: Array<IBCASOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'iboppiainecas'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBOppiaineCAS
}): IBCASSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'iboppiainecas',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBCASSuoritus',
  ...o
})

IBCASSuoritus.className = 'fi.oph.koski.schema.IBCASSuoritus' as const

export const isIBCASSuoritus = (a: any): a is IBCASSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBCASSuoritus'
