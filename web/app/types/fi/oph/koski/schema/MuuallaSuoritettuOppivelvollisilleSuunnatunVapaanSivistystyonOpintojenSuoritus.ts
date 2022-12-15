import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuuallaSuoritetutVapaanSivistystyönOpinnot } from './MuuallaSuoritetutVapaanSivistystyonOpinnot'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from './VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'

/**
 * MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus
 *
 * @see `fi.oph.koski.schema.MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus`
 */
export type MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus'
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmuuallasuoritetutopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }

export const MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
  (o: {
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi?: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstmuuallasuoritetutopinnot'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: MuuallaSuoritetutVapaanSivistystyönOpinnot
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }): MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstmuuallasuoritetutopinnot',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus',
    ...o
  })

export const isMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =
  (
    a: any
  ): a is MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus =>
    a?.$class ===
    'MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus'
