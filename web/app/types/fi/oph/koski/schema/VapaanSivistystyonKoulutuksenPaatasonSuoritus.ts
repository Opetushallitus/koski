import {
  OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus,
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
} from './OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import {
  OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022,
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
} from './OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022'
import {
  OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus,
  isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
} from './OppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import {
  VapaanSivistystyönJotpaKoulutuksenSuoritus,
  isVapaanSivistystyönJotpaKoulutuksenSuoritus
} from './VapaanSivistystyonJotpaKoulutuksenSuoritus'
import {
  VapaanSivistystyönLukutaitokoulutuksenSuoritus,
  isVapaanSivistystyönLukutaitokoulutuksenSuoritus
} from './VapaanSivistystyonLukutaitokoulutuksenSuoritus'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus
} from './VapaanSivistystyonVapaatavoitteisenKoulutuksenSuoritus'

/**
 * VapaanSivistystyönKoulutuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönKoulutuksenPäätasonSuoritus`
 */
export type VapaanSivistystyönKoulutuksenPäätasonSuoritus =
  | OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
  | OppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022
  | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
  | VapaanSivistystyönJotpaKoulutuksenSuoritus
  | VapaanSivistystyönLukutaitokoulutuksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus

export const isVapaanSivistystyönKoulutuksenPäätasonSuoritus = (
  a: any
): a is VapaanSivistystyönKoulutuksenPäätasonSuoritus =>
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus2022(
    a
  ) ||
  isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(a) ||
  isVapaanSivistystyönJotpaKoulutuksenSuoritus(a) ||
  isVapaanSivistystyönLukutaitokoulutuksenSuoritus(a) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenSuoritus(a)
