import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus } from './OppivelvollisilleSuunnattuVapaanSivistystyonOpintokokonaisuus'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from './VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'

/**
 * OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus`
 */
export type OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus'
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstopintokokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }

export const OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  (o: {
    arviointi?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi>
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'vstopintokokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
    tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
  }): OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstopintokokonaisuus',
      koodistoUri: 'suorituksentyyppi'
    }),
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus',
    ...o
  })

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus'
