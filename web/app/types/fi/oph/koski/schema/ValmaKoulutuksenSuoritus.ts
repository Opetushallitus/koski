import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { Työssäoppimisjakso } from './Tyossaoppimisjakso'
import { ValmaKoulutus } from './ValmaKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus } from './ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus'
import { HenkilövahvistusValinnaisellaPaikkakunnalla } from './HenkilovahvistusValinnaisellaPaikkakunnalla'

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
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli: ValmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
}

export const ValmaKoulutuksenSuoritus = (o: {
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'valma'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  todistuksellaNäkyvätLisätiedot?: LocalizedString
  koulutussopimukset?: Array<Koulutussopimusjakso>
  ryhmä?: string
  työssäoppimisjaksot?: Array<Työssäoppimisjakso>
  koulutusmoduuli?: ValmaKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus>
  vahvistus?: HenkilövahvistusValinnaisellaPaikkakunnalla
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

export const isValmaKoulutuksenSuoritus = (
  a: any
): a is ValmaKoulutuksenSuoritus => a?.$class === 'ValmaKoulutuksenSuoritus'
