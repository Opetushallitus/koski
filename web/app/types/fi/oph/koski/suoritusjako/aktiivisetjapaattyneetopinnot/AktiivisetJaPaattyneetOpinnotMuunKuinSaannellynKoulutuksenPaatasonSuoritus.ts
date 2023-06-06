import { AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus } from './AktiivisetJaPaattyneetOpinnotMuuKuinSaanneltyKoulutus'
import { Vahvistus } from './Vahvistus'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus'
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite
  }

export const AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =
  (o: {
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotMuuKuinSäänneltyKoulutus
    vahvistus?: Vahvistus
    toimipiste?: Toimipiste
    tyyppi: Koodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuunKuinSäännellynKoulutuksenPäätasonSuoritus'
