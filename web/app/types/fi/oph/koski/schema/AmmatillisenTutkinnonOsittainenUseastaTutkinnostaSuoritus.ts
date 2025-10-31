import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { AmmatillinenOsiaUseastaTutkinnosta } from './AmmatillinenOsiaUseastaTutkinnosta'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus } from './OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Oppija suorittaa tutkinnon osia useista tutkinnoista.
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus`
 */
export type AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainen'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: AmmatillinenOsiaUseastaTutkinnosta
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'ammatillinentutkintoosittainen'
  >
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
  suorituskieli: Koodistokoodiviite<'kieli', string>
  suoritustapa?: Koodistokoodiviite<
    'ammatillisentutkinnonsuoritustapa',
    'reformi'
  >
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli?: AmmatillinenOsiaUseastaTutkinnosta
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<OsittaisenAmmatillisenTutkinnonOsanUseastaTutkinnostaSuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): AmmatillisenTutkinnonOsittainenUseastaTutkinnostaSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ammatillinentutkintoosittainen',
    koodistoUri: 'suorituksentyyppi'
  }),
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
