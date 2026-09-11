import { SiirtoOpiskelija } from './SiirtoOpiskelija'
import { KorkeakoulunKoulutusala } from './KorkeakoulunKoulutusala'
import { KorkeakoulunOpiskeluoikeudenLukuvuosimaksu } from './KorkeakoulunOpiskeluoikeudenLukuvuosimaksu'
import { Oppilaitos } from './Oppilaitos'
import { KoulutuskuntaJakso } from './KoulutuskuntaJakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Lukukausi_Ilmoittautuminen } from './LukukausiIlmoittautuminen'
import { Liikkuvuusjakso } from './Liikkuvuusjakso'
import { Aikajakso } from './Aikajakso'
import { RahoituslähdeJakso } from './RahoituslahdeJakso'

/**
 * Korkeakoulun opiskeluoikeuden lisätiedot
 *
 * @see `fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type KorkeakoulunOpiskeluoikeudenLisätiedot = {
  $class: 'fi.oph.koski.schema.KorkeakoulunOpiskeluoikeudenLisätiedot'
  siirtoOpiskelija?: SiirtoOpiskelija
  koulutusala?: KorkeakoulunKoulutusala
  maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
  järjestäväOrganisaatio?: Oppilaitos
  koulutuskuntaJaksot: Array<KoulutuskuntaJakso>
  opettajanPedagogisetOpinnot?: Array<
    Koodistokoodiviite<'virtapatevyys', string>
  >
  lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
  liikkuvuusjaksot?: Array<Liikkuvuusjakso>
  opetettavanAineenOpinnot?: Array<Koodistokoodiviite<'virtapatevyys', string>>
  ensisijaisuus?: Array<Aikajakso>
  virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
    'virtaopiskeluoikeudentyyppi',
    string
  >
  rahoituslähdeJaksot?: Array<RahoituslähdeJakso>
}

export const KorkeakoulunOpiskeluoikeudenLisätiedot = (
  o: {
    siirtoOpiskelija?: SiirtoOpiskelija
    koulutusala?: KorkeakoulunKoulutusala
    maksettavatLukuvuosimaksut?: Array<KorkeakoulunOpiskeluoikeudenLukuvuosimaksu>
    järjestäväOrganisaatio?: Oppilaitos
    koulutuskuntaJaksot?: Array<KoulutuskuntaJakso>
    opettajanPedagogisetOpinnot?: Array<
      Koodistokoodiviite<'virtapatevyys', string>
    >
    lukukausiIlmoittautuminen?: Lukukausi_Ilmoittautuminen
    liikkuvuusjaksot?: Array<Liikkuvuusjakso>
    opetettavanAineenOpinnot?: Array<
      Koodistokoodiviite<'virtapatevyys', string>
    >
    ensisijaisuus?: Array<Aikajakso>
    virtaOpiskeluoikeudenTyyppi?: Koodistokoodiviite<
      'virtaopiskeluoikeudentyyppi',
      string
    >
    rahoituslähdeJaksot?: Array<RahoituslähdeJakso>
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
