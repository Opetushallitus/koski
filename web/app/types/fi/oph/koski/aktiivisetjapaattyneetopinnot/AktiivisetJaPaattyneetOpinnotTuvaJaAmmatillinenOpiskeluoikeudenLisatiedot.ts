import { AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso } from './AktiivisetJaPaattyneetOpinnotOsaAikaisuusJakso'

/**
 * AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot`
 */
export type AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot'
    osaAikaisuusjaksot?: Array<AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso>
  }

export const AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot =
  (
    o: {
      osaAikaisuusjaksot?: Array<AktiivisetJaPäättyneetOpinnotOsaAikaisuusJakso>
    } = {}
  ): AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot',
    ...o
  })

AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot' as const

export const isAktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTuvaJaAmmatillinenOpiskeluoikeudenLisätiedot'
