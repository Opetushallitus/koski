import { IBCoreKurssinArviointi } from './IBCoreKurssinArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBCoreKurssi } from './IBCoreKurssi'

/**
 * IBCoreKurssinSuoritus
 *
 * @see `fi.oph.koski.schema.IBCoreKurssinSuoritus`
 */
export type IBCoreKurssinSuoritus = {
  $class: 'fi.oph.koski.schema.IBCoreKurssinSuoritus'
  arviointi?: Array<IBCoreKurssinArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibcorekurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBCoreKurssi
}

export const IBCoreKurssinSuoritus = (o: {
  arviointi?: Array<IBCoreKurssinArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibcorekurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBCoreKurssi
}): IBCoreKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibcorekurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBCoreKurssinSuoritus',
  ...o
})

IBCoreKurssinSuoritus.className =
  'fi.oph.koski.schema.IBCoreKurssinSuoritus' as const

export const isIBCoreKurssinSuoritus = (a: any): a is IBCoreKurssinSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBCoreKurssinSuoritus'
