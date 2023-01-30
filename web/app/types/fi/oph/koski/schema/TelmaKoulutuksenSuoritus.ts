import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { TelmaKoulutus } from './TelmaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { TelmaKoulutuksenOsanSuoritus } from './TelmaKoulutuksenOsanSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Työhön ja itsenäiseen elämään valmentava koulutus (TELMA)
 *
 * @see `fi.oph.koski.schema.TelmaKoulutuksenSuoritus`
 */
export type TelmaKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.TelmaKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'telma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: TelmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TelmaKoulutuksenOsanSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const TelmaKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'telma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli?: TelmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<TelmaKoulutuksenOsanSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}): TelmaKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'telma',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: TelmaKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999903',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.TelmaKoulutuksenSuoritus',
  ...o
})

TelmaKoulutuksenSuoritus.className =
  'fi.oph.koski.schema.TelmaKoulutuksenSuoritus' as const

export const isTelmaKoulutuksenSuoritus = (
  a: any
): a is TelmaKoulutuksenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.TelmaKoulutuksenSuoritus'
