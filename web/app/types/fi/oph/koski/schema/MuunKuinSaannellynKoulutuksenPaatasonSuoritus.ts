import { MuunKuinSäännellynKoulutuksenArviointi } from './MuunKuinSaannellynKoulutuksenArviointi'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { MuuKuinSäänneltyKoulutus } from './MuuKuinSaanneltyKoulutus'
import { OrganisaatioWithOid } from './OrganisaatioWithOid'
import { MuunKuinSäännellynKoulutuksenOsasuoritus } from './MuunKuinSaannellynKoulutuksenOsasuoritus'
import { Päivämäärävahvistus } from './Paivamaaravahvistus'

/**
 * MuunKuinSäännellynKoulutuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus`
 */
export type MuunKuinSäännellynKoulutuksenPäätasonSuoritus = {
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus'
  arviointi?: Array<MuunKuinSäännellynKoulutuksenArviointi>
  tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'muukuinsaanneltykoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKuinSäänneltyKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MuunKuinSäännellynKoulutuksenOsasuoritus>
  vahvistus?: Päivämäärävahvistus
}

export const MuunKuinSäännellynKoulutuksenPäätasonSuoritus = (o: {
  arviointi?: Array<MuunKuinSäännellynKoulutuksenArviointi>
  tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'muukuinsaanneltykoulutus'>
  tila?: Koodistokoodiviite<'suorituksentila', string>
  suorituskieli: Koodistokoodiviite<'kieli', string>
  koulutusmoduuli: MuuKuinSäänneltyKoulutus
  toimipiste: OrganisaatioWithOid
  osasuoritukset?: Array<MuunKuinSäännellynKoulutuksenOsasuoritus>
  vahvistus?: Päivämäärävahvistus
}): MuunKuinSäännellynKoulutuksenPäätasonSuoritus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'muukuinsaanneltykoulutus',
    koodistoUri: 'suorituksentyyppi'
  }),
  $class: 'fi.oph.koski.schema.MuunKuinSäännellynKoulutuksenPäätasonSuoritus',
  ...o
})

export const isMuunKuinSäännellynKoulutuksenPäätasonSuoritus = (
  a: any
): a is MuunKuinSäännellynKoulutuksenPäätasonSuoritus =>
  a?.$class === 'MuunKuinSäännellynKoulutuksenPäätasonSuoritus'
