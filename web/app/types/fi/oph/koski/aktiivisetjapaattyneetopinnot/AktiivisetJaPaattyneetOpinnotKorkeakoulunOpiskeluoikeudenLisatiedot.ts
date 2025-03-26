import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso } from './AktiivisetJaPaattyneetOpinnotKoulutuskuntaJakso'
import { AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen } from './AktiivisetJaPaattyneetOpinnotLukukausiIlmoittautuminen'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
    opetettavanAineenOpinnot?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    koulutuskuntaJaksot: Array<AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso>
    opettajanPedagogisetOpinnot?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
    virtaOpiskeluoikeudenTyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    lukukausiIlmoittautuminen?: AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
  }

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  (
    o: {
      opetettavanAineenOpinnot?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
      koulutuskuntaJaksot?: Array<AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso>
      opettajanPedagogisetOpinnot?: Array<AktiivisetJaPäättyneetOpinnotKoodistokoodiviite>
      virtaOpiskeluoikeudenTyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
      lukukausiIlmoittautuminen?: AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
    } = {}
  ): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot => ({
    koulutuskuntaJaksot: [],
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot',
    ...o
  })

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
