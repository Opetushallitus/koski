import { Järjestämismuotojakso } from './Jarjestamismuotojakso'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { AmmatillinenOsiaUseastaTutkinnosta } from './AmmatillinenOsiaUseastaTutkinnosta'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus } from './OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { Osaamisalajakso } from './Osaamisalajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Oppija suorittaa tutkinnon osia useista tutkinnoista.
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
  suoritustapa: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: AmmatillinenOsiaUseastaTutkinnosta
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
  suoritustapa?: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli?: AmmatillinenOsiaUseastaTutkinnosta
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
  suoritustapa: Koodistokoodiviite({
    koodiarvo: 'reformi',
    koodistoUri: 'ammatillisentutkinnonsuoritustapa'
  }),
  koulutusmoduuli: AmmatillinenOsiaUseastaTutkinnosta({
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
