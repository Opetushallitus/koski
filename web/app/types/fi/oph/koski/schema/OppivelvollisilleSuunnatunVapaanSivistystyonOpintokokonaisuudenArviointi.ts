import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi`
 */
export type OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi'
    arvosana: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    päivä: string
    hyväksytty?: boolean
  }

export const OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =
  (o: {
    arvosana?: Koodistokoodiviite<'arviointiasteikkovst', 'Hyväksytty'>
    päivä: string
    hyväksytty?: boolean
  }): OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi => ({
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi',
    arvosana: Koodistokoodiviite({
      koodiarvo: 'Hyväksytty',
      koodistoUri: 'arviointiasteikkovst'
    }),
    ...o
  })

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi =>
    a?.$class ===
    'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenArviointi'
