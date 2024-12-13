import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen } from './AktiivisetJaPaattyneetOpinnotLukukausiIlmoittautuminen'
import { AktiivisetJaPäättyneetOpinnotKoulutuskuntaJakso } from './AktiivisetJaPaattyneetOpinnotKoulutuskuntaJakso'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
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
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot',
    koulutuskuntaJaksot: [],
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
