import { LukionOppiaineenArviointi } from './LukionOppiaineenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuuLukioOpinto2015 } from './MuuLukioOpinto2015'
import { LukionKurssinSuoritus2015 } from './LukionKurssinSuoritus2015'

/**
 * Kategoria kursseille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esimerkiksi lukiodiplomi, taiteiden väliset opinnot, teemaopinnot
 *
 * @see `fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2015`
 */
export type MuidenLukioOpintojenSuoritus2015 = {
  $class: 'fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2015'
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuuLukioOpinto2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}

export const MuidenLukioOpintojenSuoritus2015 = (o: {
  arviointi?: Array<LukionOppiaineenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  koulutusmoduuli: MuuLukioOpinto2015
  osasuoritukset?: Array<LukionKurssinSuoritus2015>
}): MuidenLukioOpintojenSuoritus2015 => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionmuuopinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2015',
  ...o
})

export const isMuidenLukioOpintojenSuoritus2015 = (
  a: any
): a is MuidenLukioOpintojenSuoritus2015 =>
  a?.$class === 'fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2015'
