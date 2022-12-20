import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { TutkintokoulutukseenValmentavanKoulutus } from './TutkintokoulutukseenValmentavanKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus } from './TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Tutkintokoulutukseen valmentavan koulutuksen suoritustiedot.
 *
 * @see `fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus`
 */
export type TutkintokoulutukseenValmentavanKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'tuvakoulutuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const TutkintokoulutukseenValmentavanKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'tuvakoulutuksensuoritus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutusmoduuli?: TutkintokoulutukseenValmentavanKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TutkintokoulutukseenValmentavanKoulutuksenOsanSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): TutkintokoulutukseenValmentavanKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'tuvakoulutuksensuoritus',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: TutkintokoulutukseenValmentavanKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999908',
      koodistoUri: 'koulutus'
    })
  }),
  $class:
    'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus',
  ...o
})

export const isTutkintokoulutukseenValmentavanKoulutuksenSuoritus = (
  a: any
): a is TutkintokoulutukseenValmentavanKoulutuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.schema.TutkintokoulutukseenValmentavanKoulutuksenSuoritus'
