import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuutSuorituksetTaiVastaavat2019 } from './MuutSuorituksetTaiVastaavat2019'
import { LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 } from './LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019'

/**
 * Kategoria opintojaksoille, jotka eivät liity suoraan mihinkään yksittäiseen oppiaineeseen. Esim. lukiodiplomit tai temaattiset opinnot.
 *
 * @see `fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2019`
 */
export type MuidenLukioOpintojenSuoritus2019 = {
  $class: 'fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2019'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  koulutusmoduuli: MuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}

export const MuidenLukioOpintojenSuoritus2019 = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'lukionmuuopinto'>
  koulutusmoduuli: MuutSuorituksetTaiVastaavat2019
  osasuoritukset?: Array<LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019>
  tila?: Koodistokoodiviite<'suorituksentila', string>
}): MuidenLukioOpintojenSuoritus2019 => ({
  $class: 'fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2019',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'lukionmuuopinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

export const isMuidenLukioOpintojenSuoritus2019 = (
  a: any
): a is MuidenLukioOpintojenSuoritus2019 =>
  a?.$class === 'fi.oph.koski.schema.MuidenLukioOpintojenSuoritus2019'
