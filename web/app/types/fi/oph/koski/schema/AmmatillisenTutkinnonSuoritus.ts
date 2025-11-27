import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { AmmatillisenTutkinnonOsanSuoritus } from './AmmatillisenTutkinnonOsanSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Suoritettavan ammatillisen tutkinnon tiedot
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonSuoritus`
 */
export type AmmatillisenTutkinnonSuoritus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonSuoritus'
  suorituskieli: Koodistokoodiviite<'kieli', string>
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'ammatillinentutkinto'>
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const AmmatillisenTutkinnonSuoritus = (o: {
  suorituskieli: Koodistokoodiviite<'kieli', string>
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<AmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'ammatillinentutkinto'>
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): AmmatillisenTutkinnonSuoritus => ({
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonSuoritus',
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinentutkinto',
    koodistoUri: 'suorituksentyyppi'
  }),
  ...o
})

AmmatillisenTutkinnonSuoritus.className =
  'fi.oph.koski.schema.AmmatillisenTutkinnonSuoritus' as const

export const isAmmatillisenTutkinnonSuoritus = (
  a: any
): a is AmmatillisenTutkinnonSuoritus =>
  a?.$class === 'fi.oph.koski.schema.AmmatillisenTutkinnonSuoritus'
