import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli } from './SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { SuoritetutTutkinnotOsaamisalajakso } from './SuoritetutTutkinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus`
 */
export type SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus'
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkinto'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = (o: {
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkinto'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutussopimukset?: Array<Koulutussopimusjakso>
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus',
  ...o
})

SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus' as const

export const isSuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = (
  a: any
): a is SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus'
