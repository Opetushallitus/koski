import {
  AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus,
  isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
} from './AktiivisetJaPaattyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus'
import {
  AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus,
  isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
} from './AktiivisetJaPaattyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyonKoulutuksenSuoritus'
import {
  AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus,
  isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus
} from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonJotpaKoulutuksenSuoritus'
import {
  AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus,
  isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus
} from './AktiivisetJaPaattyneetOpinnotVapaanSivistystyonLukutaitokoulutuksenSuoritus'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus =
  | AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus
  | AktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus
  | AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus
  | AktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönPäätasonSuoritus =>
    isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuMaahanmuuttajienKotoutumiskoulutuksenSuoritus(
      a
    ) ||
    isAktiivisetJaPäättyneetOpinnotOppivelvollisilleSuunnattuVapaanSivistystyönKoulutuksenSuoritus(
      a
    ) ||
    isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutuksenSuoritus(
      a
    ) ||
    isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönLukutaitokoulutuksenSuoritus(
      a
    )
