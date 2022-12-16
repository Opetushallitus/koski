import { Aikajakso } from './Aikajakso'
import { KorkeakoulunOpiskeluoikeudenLukuvuosimaksu } from './KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'
import { Oppilaitos } from './Oppilaitos'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Lukukausi_Ilmoittautuminen } from './LukukausiIlmoittautuminen'

/**
 * Korkeakoulun opiskeluoikeuden lisätiedot
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type KorkeakoulunOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot'
  ensisijaisuus?: Array<Aikajakso>
  maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
  järjestäväOrganisaatio?: Oppilaitos
  virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
    'virtaopiskeluoikeudentyyppi',
    string
  >
  lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
}

export const KorkeakoulunOpiskeluoikeudenLisätiedot = (
  o: {
    ensisijaisuus?: Array<Aikajakso>
    maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
    järjestäväOrganisaatio?: Oppilaitos
    virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
      'virtaopiskeluoikeudentyyppi',
      string
    >
    lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
  } = {}
): KorkeakoulunOpiskeluoikeudenLisätiedot => ({
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot',
  ...o
})

export const isKorkeakoulunOpiskeluoikeudenLisätiedot = (
  a: any
): a is KorkeakoulunOpiskeluoikeudenLisätiedot =>
  a?.$class === 'KorkeakoulunOpiskeluoikeudenLisätiedot'
