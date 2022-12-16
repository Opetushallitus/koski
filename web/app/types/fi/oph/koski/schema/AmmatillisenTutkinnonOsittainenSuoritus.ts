import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { AmmatillinenTutkintoKoulutus } from './AmmatillinenTutkintoKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OsittaisenAmmatillisenTutkinnonOsanSuoritus } from './OsittaisenAmmatillisenTutkinnonOsanSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Oppija suorittaa yhtä tai useampaa tutkinnon osaa, eikä koko tutkintoa. Mikäli opiskelija suorittaa toista osaamisalaa tai tutkintonimikettä erillisessä opiskeluoikeudessa, välitään tieto tällöin tämän rakenteen kautta
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenSuoritus`
 */
export type AmmatillisenTutkinnonOsittainenSuoritus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenSuoritus'
  toinenTutkintonimike: boolean
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainen'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  toinenOsaamisala: boolean
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const AmmatillisenTutkinnonOsittainenSuoritus = (o: {
  toinenTutkintonimike?: boolean
  järjestämismuodot?: Array<Järjestämismuotojakso>
  tutkintonimike?: Array<Koodistokoodiviite<'tutkintonimikkeet', string>>
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainen'
  >
  keskiarvo?: number
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  toinenOsaamisala?: boolean
  keskiarvoSisältääMukautettujaArvosanoja?: boolean
  suoritustapa: Koodistokoodiviite<'ammatillisentutkinnonsuoritustapa', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: AmmatillinenTutkintoKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): AmmatillisenTutkinnonOsittainenSuoritus => ({
  toinenTutkintonimike: false,
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinentutkintoosittainen',
    koodistoUri: 'suorituksentyyppi'
  }),
  toinenOsaamisala: false,
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenSuoritus',
  ...o
})

export const isAmmatillisenTutkinnonOsittainenSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsittainenSuoritus =>
  a?.$class === 'AmmatillisenTutkinnonOsittainenSuoritus'
