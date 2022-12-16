import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot`
 */
export type OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot'
    tunniste: Koodistokoodiviite<
      'vstmuutopinnot',
      'valinnaisetsuuntautumisopinnot'
    >
    laajuus?: LaajuusOpintopisteissä
  }

export const OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =
  (
    o: {
      tunniste?: Koodistokoodiviite<
        'vstmuutopinnot',
        'valinnaisetsuuntautumisopinnot'
      >
      laajuus?: LaajuusOpintopisteissä
    } = {}
  ): OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot => ({
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot',
    tunniste: Koodistokoodiviite({
      koodiarvo: 'valinnaisetsuuntautumisopinnot',
      koodistoUri: 'vstmuutopinnot'
    }),
    ...o
  })

export const isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot =>
    a?.$class ===
    'OppivelvollisilleSuunnatunVapaanSivistystyönValinnaisetSuuntautumisopinnot'
