import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { AmmatillinenOsaTaiOsiaUseastaTutkinnosta } from './AmmatillinenOsaTaiOsiaUseastaTutkinnosta'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus } from './OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Oppija suorittaa yhtä tutkinnon osaa toisesta tutkinnosta tai useampaa tutkinnon osaa toisista tutkinnoista.
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus`
 */
export type AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
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
  koulutusmoduuli: AmmatillinenOsaTaiOsiaUseastaTutkinnosta
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = (o: {
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
  koulutusmoduuli?: AmmatillinenOsaTaiOsiaUseastaTutkinnosta
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  osaamisala?: Array<Osaamisalajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus => ({
  toinenTutkintonimike: false,
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinentutkintoosittainen',
    koodistoUri: 'suorituksentyyppi'
  }),
  toinenOsaamisala: false,
  koulutusmoduuli: AmmatillinenOsaTaiOsiaUseastaTutkinnosta({
    tunniste: Koodistokoodiviite({
      koodiarvo: 'ammatillinentutkintoosittainenuseastatutkinnosta',
      koodistoUri: 'suorituksentyyppi'
    })
  }),
  $class:
    'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus',
  ...o
})

AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus.className =
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus' as const

export const isAmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = (
  a: any
): a is AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
