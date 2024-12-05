import {
  AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus,
  isAikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus
} from './AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus'
import {
  MuunKuinSäännellynKoulutuksenOsasuoritus,
  isMuunKuinSäännellynKoulutuksenOsasuoritus
} from './MuunKuinSaannellynKoulutuksenOsasuoritus'
import {
  MuunKuinSäännellynKoulutuksenPäätasonSuoritus,
  isMuunKuinSäännellynKoulutuksenPäätasonSuoritus
} from './MuunKuinSaannellynKoulutuksenPaatasonSuoritus'
import {
  VSTKotoutumiskoulutuksenOhjauksenSuoritus2022,
  isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022
} from './VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'

/**
 * MahdollisestiArvioinniton
 *
 * @see `fi.oph.koski.schema.MahdollisestiArvioinniton`
 */
export type MahdollisestiArvioinniton =
  | AikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus
  | MuunKuinSäännellynKoulutuksenOsasuoritus
  | MuunKuinSäännellynKoulutuksenPäätasonSuoritus
  | VSTKotoutumiskoulutuksenOhjauksenSuoritus2022

export const isMahdollisestiArvioinniton = (
  a: any
): a is MahdollisestiArvioinniton =>
  isAikuistenPerusopetuksenAlkuvaiheenOppiaineenSuoritus(a) ||
  isMuunKuinSäännellynKoulutuksenOsasuoritus(a) ||
  isMuunKuinSäännellynKoulutuksenPäätasonSuoritus(a) ||
  isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022(a)
