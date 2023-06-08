import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus } from './AktiivisetJaPaattyneetOpinnotTutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaKoulutus'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Vahvistus } from './Vahvistus'

/**
 * AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus'
    tyyppi: Koodistokoodiviite<
      string,
      'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
    >
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    vahvistus?: Vahvistus
  }

export const AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =
  (o: {
    tyyppi: Koodistokoodiviite<
      string,
      'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
    >
    koulutussopimukset?: Array<Koulutussopimusjakso>
    koulutusmoduuli: AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
    toimipiste?: Toimipiste
    osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
    vahvistus?: Vahvistus
  }): AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus' as const

export const isAktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus'
