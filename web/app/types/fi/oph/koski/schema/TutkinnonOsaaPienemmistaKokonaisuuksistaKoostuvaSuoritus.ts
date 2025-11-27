import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus } from './TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus } from './TutkinnonOsaaPienemmistaKokonaisuuksistaKoostuvanSuorituksenOsasuoritus'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus
 *
 * @see `fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus`
 */
export type TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = {
  $class: 'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus'
  tyyppi: Koodistokoodiviite<
    'suorituksentyyppi',
    'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}

export const TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<
    'suorituksentyyppi',
    'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus'
  >
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  koulutusmoduuli: TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvanSuorituksenOsasuoritus>
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  tila?: Koodistokoodiviite<'suorituksentila', string>
  alkamispäivä?: string
}): TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'tutkinnonosaapienemmistäkokonaisuuksistakoostuvasuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class:
    'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus',
  ...o
})

TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus.className =
  'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus' as const

export const isTutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus = (
  a: any
): a is TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkinnonOsaaPienemmistäKokonaisuuksistaKoostuvaSuoritus'
