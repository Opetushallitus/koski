import { KorkeakoulunOpiskeluoikeudenLukuvuosimaksu } from './KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'
import { Oppilaitos } from './Oppilaitos'
import { KoulutuskuntaJakso } from './KoulutuskuntaJakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Lukukausi_Ilmoittautuminen } from './LukukausiIlmoittautuminen'
import { Aikajakso } from './Aikajakso'

/**
 * Korkeakoulun opiskeluoikeuden lisätiedot
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type KorkeakoulunOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot'
  maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
  järjestäväOrganisaatio?: Oppilaitos
  koulutuskuntaJaksot: Array<KoulutuskuntaJakso>
  opettajanPedagogisetOpinnot?: Array<
    Koodistokoodiviite<'virtapatevyys', string>
  >
  virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
    'virtaopiskeluoikeudentyyppi',
    string
  >
  lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
  opetettavanAineenOpinnot?: Array<Koodistokoodiviite<'virtapatevyys', string>>
  ensisijaisuus?: Array<Aikajakso>
}

export const KorkeakoulunOpiskeluoikeudenLisätiedot = (
  o: {
    maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
    järjestäväOrganisaatio?: Oppilaitos
    koulutuskuntaJaksot?: Array<KoulutuskuntaJakso>
    opettajanPedagogisetOpinnot?: Array<
      Koodistokoodiviite<'virtapatevyys', string>
    >
    virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
      'virtaopiskeluoikeudentyyppi',
      string
    >
    lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
    opetettavanAineenOpinnot?: Array<
      Koodistokoodiviite<'virtapatevyys', string>
    >
    ensisijaisuus?: Array<Aikajakso>
  } = {}
): KorkeakoulunOpiskeluoikeudenLisätiedot => ({
  koulutuskuntaJaksot: [],
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot',
  ...o
})

KorkeakoulunOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot' as const

export const isKorkeakoulunOpiskeluoikeudenLisätiedot = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenLisätiedot =>
  a?.$class === 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot'
