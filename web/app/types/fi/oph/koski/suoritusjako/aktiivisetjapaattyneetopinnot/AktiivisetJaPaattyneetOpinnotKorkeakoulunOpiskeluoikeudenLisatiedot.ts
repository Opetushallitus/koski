import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen } from './AktiivisetJaPaattyneetOpinnotLukukausiIlmoittautuminen'
import { AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso } from './AktiivisetJaPaattyneetOpinnotKoulutuskuntaJakso'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
    virtaOpiskeluoikeudenTyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    lukukausiIlmoittautuminen?: AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
    koulutuskuntaJaksot: Array<AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso>
  }

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  (
    o: {
      virtaOpiskeluoikeudenTyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
      lukukausiIlmoittautuminen?: AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
      koulutuskuntaJaksot?: Array<AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso>
    } = {}
  ): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot',
    koulutuskuntaJaksot: [],
    ...o
  })

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
