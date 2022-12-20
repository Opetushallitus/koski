import { LukionArviointi } from './LukionArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LukionKurssi2015 } from './LukionKurssi2015'
import { OsaamisenTunnustaminen } from './OsaamisenTunnustaminen'

/**
 * Lukion kurssin suoritustiedot
 *
 * @see `fi.oph.koski.schema.LukionKurssinSuoritus2015`
 */
export type LukionKurssinSuoritus2015 = {
  $class: 'fi.oph.koski.schema.LukionKurssinSuoritus2015'
  arviointi?: Array<LukionArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionkurssi'>
  suoritettuLukiodiplomina?: boolean
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuSuullisenaKielikokeena?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionKurssi2015
  tunnustettu?: OsaamisenTunnustaminen
}

export const LukionKurssinSuoritus2015 = (o: {
  arviointi?: Array<LukionArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionkurssi'>
  suoritettuLukiodiplomina?: boolean
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suoritettuSuullisenaKielikokeena?: boolean
  suorituskieli?: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: LukionKurssi2015
  tunnustettu?: OsaamisenTunnustaminen
}): LukionKurssinSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionkurssi',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.LukionKurssinSuoritus2015',
  ...o
})

export const isLukionKurssinSuoritus2015 = (
  a: any
): a is LukionKurssinSuoritus2015 =>
  a?.$class === 'fi.oph.koski.schema.LukionKurssinSuoritus2015'
