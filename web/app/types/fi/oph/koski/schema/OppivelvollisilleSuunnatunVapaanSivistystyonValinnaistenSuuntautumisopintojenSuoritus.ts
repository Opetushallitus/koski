import { OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot } from './OppivelvollisilleSuunnatunVapaanSivistystyonValinnaisetSuuntautumisopinnot'
import { VapaanSivistystyönOpintokokonaisuudenSuoritus } from './VapaanSivistystyonOpintokokonaisuudenSuoritus'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus`
 */
export type OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus'
    koulutusmoduuli: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot
    osasuoritukset?: Array<VapaanSivistystyönOpintokokonaisuudenSuoritus>
    tyyppi: Koodistokoodiviite<
      'suorituksentyyppi',
      'vstvalinnainensuuntautuminen'
    >
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
  (
    o: {
      koulutusmoduuli?: OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot
      osasuoritukset?: Array<VapaanSivistystyönOpintokokonaisuudenSuoritus>
      tyyppi?: Koodistokoodiviite<
        'suorituksentyyppi',
        'vstvalinnainensuuntautuminen'
      >
      tila?: Koodistokoodiviite<'suorituksentila', string>
    } = {}
  ): OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus => ({
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstvalinnainensuuntautuminen',
      koodistoUri: 'suorituksentyyppi'
    }),
    koulutusmoduuli:
      OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot(
        {
          tunniste: Koodistokoodiviite({
            koodiarvo: 'valinnaisetsuuntautumisopinnot',
            koodistoUri: 'vstmuutopinnot'
          })
        }
      ),
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus',
    ...o
  })

export const isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus'
