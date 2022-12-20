import { IBKurssinArviointi } from './IBKurssinArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { IBKurssi } from './IBKurssi'

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
}

export const IBKurssinSuoritus = (o: {
  arviointi?: Array<IBKurssinArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ibkurssi'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: IBKurssi
}): IBKurssinSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ibkurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.IBKurssinSuoritus',
  ...o
})

export const isIBKurssinSuoritus = (a: any): a is IBKurssinSuoritus =>
  a?.$class === 'fi.oph.koski.schema.IBKurssinSuoritus'
