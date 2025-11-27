import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { ValmaKoulutus } from './ValmaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus } from './ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'

/**
 * Laajennetun oppivelvollisuuden suoritus
 * Ammatilliseen peruskoulutukseen valmentava koulutus (VALMA)
 *
 * @see `fi.oph.koski.schema.ValmaKoulutuksenSuoritus`
 */
export type ValmaKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.schema.ValmaKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'valma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: ValmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
}

export const ValmaKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'valma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli?: ValmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
}): ValmaKoulutuksenSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'valma',
    koodistoUri: 'suorituksentyyppi'
  }),
  koulutusmoduuli: ValmaKoulutus({
    tunniste: Koodistokoodiviite({
      koodiarvo: '999901',
      koodistoUri: 'koulutus'
    })
  }),
  $class: 'fi.oph.koski.schema.ValmaKoulutuksenSuoritus',
  ...o
})

ValmaKoulutuksenSuoritus.className =
  'fi.oph.koski.schema.ValmaKoulutuksenSuoritus' as const

export const isValmaKoulutuksenSuoritus = (
  a: any
): a is ValmaKoulutuksenSuoritus =>
  a?.$class === 'fi.oph.koski.schema.ValmaKoulutuksenSuoritus'
