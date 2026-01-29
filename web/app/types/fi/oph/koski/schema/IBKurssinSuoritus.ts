import { IBKurssinArviointi } from './IBKurssinArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBKurssi } from './IBKurssi'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * IBKurssinSuoritus
 *
 * @see `fi.oph.koski.schema.IBKurssinSuoritus`
 */
export type IBKurssinSuoritus = {
  $class: 'fi.oph.koski.schema.IBKurssinSuoritus'
  arviointi?: Array<IBKurssinArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBKurssi
  tunnustettu?: OsaamisenTunnustaminen
}

export const IBKurssinSuoritus = (o: {
  arviointi?: Array<IBKurssinArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBKurssi
  tunnustettu?: OsaamisenTunnustaminen
}): IBKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibkurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBKurssinSuoritus',
  ...o
})

IBKurssinSuoritus.className = 'fi.oph.koski.schema.IBKurssinSuoritus' as const

export const isIBKurssinSuoritus = (a: any): a is IBKurssinSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBKurssinSuoritus'
