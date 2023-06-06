import { AktiivisetJaPäättyneetOpinnotLaajuus } from './AktiivisetJaPaattyneetOpinnotLaajuus'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotKoodiViite } from './AktiivisetJaPaattyneetOpinnotKoodiViite'

/**
 * AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli'
    laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
    perusteenNimi?: LocalizedString
    kuvaus?: LocalizedString
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    tunniste: AktiivisetJaPäättyneetOpinnotKoodiViite
  }

export const AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli =
  (o: {
    laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
    perusteenNimi?: LocalizedString
    kuvaus?: LocalizedString
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    tunniste: AktiivisetJaPäättyneetOpinnotKoodiViite
  }): AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli',
    ...o
  })

AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotAmmatillisenSuorituksenKoulutusmoduuli'
