import { Arviointi } from '../types/fi/oph/koski/schema/Arviointi'
import { LukutaitokoulutuksenArviointi } from '../types/fi/oph/koski/schema/LukutaitokoulutuksenArviointi'
import {
  isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus,
  OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnattuVapaanSivistystyonKoulutus'
import {
  OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus,
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
} from '../types/fi/oph/koski/schema/OppivelvollisilleSuunnatunVapaanSivistystyonOsasuoritus'
import {
  VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus,
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenOsasuorituksenSuoritus'
import {
  VapaanSivistystyönJotpaKoulutus,
  isVapaanSivistystyönJotpaKoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutus'
import {
  VapaanSivistystyönLukutaitokoulutus,
  isVapaanSivistystyönLukutaitokoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonLukutaitokoulutus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenTyoelamaJaYhteiskuntataitojenOpintojenSuoritus'
import {
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus,
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus'
import {
  VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus,
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonMaahanmuuttajienKotoutumiskoulutus'
import { VapaanSivistystyönPäätasonSuoritus } from '../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import {
  VapaanSivistystyönVapaatavoitteinenKoulutus,
  isVapaanSivistystyönVapaatavoitteinenKoulutus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteinenKoulutus'
import {
  VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus,
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
} from '../types/fi/oph/koski/schema/VapaanSivistystyonVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus'
import {
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022,
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import {
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022,
  VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import {
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022,
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'
import {
  VSTKotoutumiskoulutus2022,
  isVSTKotoutumiskoulutus2022
} from '../types/fi/oph/koski/schema/VSTKotoutumiskoulutus2022'

export const isLaajuuksellinenVSTKoulutusmoduuli = (
  x: any
): x is
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
  | VSTKotoutumiskoulutus2022
  | VapaanSivistystyönJotpaKoulutus
  | VapaanSivistystyönLukutaitokoulutus
  | VapaanSivistystyönVapaatavoitteinenKoulutus => {
  return (
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(x) ||
    isVSTKotoutumiskoulutus2022(x) ||
    isVapaanSivistystyönJotpaKoulutus(x) ||
    isVapaanSivistystyönLukutaitokoulutus(x) ||
    isVapaanSivistystyönVapaatavoitteinenKoulutus(x)
  )
}

export const isPerusteellinenVSTKoulutusmoduuli = (
  x: any
): x is
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
  | VSTKotoutumiskoulutus2022
  | OppivelvollisilleSuunnattuVapaanSivistystyönKoulutus
  | VapaanSivistystyönLukutaitokoulutus => {
  return (
    isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus(x) ||
    isVSTKotoutumiskoulutus2022(x) ||
    isOppivelvollisilleSuunnattuVapaanSivistystyönKoulutus(x) ||
    isVapaanSivistystyönLukutaitokoulutus(x)
  )
}

type OsasuoritusOf<T> = T extends { osasuoritukset?: any }
  ? NonNullable<T['osasuoritukset']>[number]
  : never

export type VSTOsasuoritus =
  | OsasuoritusOf<VapaanSivistystyönPäätasonSuoritus>
  | OsasuoritusOf<OsasuoritusOf<VapaanSivistystyönPäätasonSuoritus>>

export type VSTOsasuoritusOsasuorituksilla = Extract<
  VSTOsasuoritus,
  {
    osasuoritukset?: any
  }
>

export function isVSTOsasuoritusOsasuorituksilla(
  x: VSTOsasuoritus
): x is VSTOsasuoritusOsasuorituksilla {
  return true
}

export type VSTOsasuoritusArvioinnilla = Extract<
  VSTOsasuoritus,
  {
    arviointi?: Arviointi[]
  }
>

export type VSTOsasuoritusLukutaitokoulutuksenArvioinnilla = Extract<
  VSTOsasuoritus,
  {
    arviointi?: LukutaitokoulutuksenArviointi[]
  }
>

export type VSTOsasuoritusIlmanArviointia = Exclude<
  VSTOsasuoritus,
  {
    arviointi?: Arviointi[]
  }
>

export function hasArviointi(
  s: VSTOsasuoritus
): s is VSTOsasuoritusArvioinnilla {
  switch (s.$class) {
    case 'fi.oph.koski.schema.MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus':
    case 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus':
    case 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenOsasuoritus':
    case 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022':
    case 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022':
    case 'fi.oph.koski.schema.VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022':
    case 'fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönLukutaitokoulutuksenKokonaisuudenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenKieliopintojenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenOhjauksenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenTyöelämäJakso':
    case 'fi.oph.koski.schema.VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus':
      return true
    default:
      return false
  }
}

/*
export function hasNoArviointi(
  s: VSTOsasuoritus
): s is VSTOsasuoritusIlmanArviointia {
  return !hasArviointi(s)
}
*/

export type VSTArviointi = NonNullable<
  VSTOsasuoritusArvioinnilla['arviointi']
>[number]

export type VSTTaitotaso = NonNullable<
  NonNullable<
    VSTOsasuoritusLukutaitokoulutuksenArvioinnilla['arviointi']
  >[number]['taitotaso']
>

export type CreateVSTArviointi = (
  o: VSTOsasuoritusArvioinnilla
) => (arvosana: any) => VSTArviointi | null

export type CreateVSTTaitotaso = (
  o: VSTOsasuoritusLukutaitokoulutuksenArvioinnilla
) => (arvosana: any) => VSTTaitotaso | null

export const hasOsasuoritustenOsasuorituksia = (
  s: any
): s is
  | VapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus
  | VapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus
  | VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  | VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
  | VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  | OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus
  | VapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus =>
  isVapaanSivistystyönJotpaKoulutuksenOsasuorituksenSuoritus(s) ||
  isVapaanSivistystyönVapaatavoitteisenKoulutuksenOsasuorituksenSuoritus(s) ||
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(s) ||
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(s) ||
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(s) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus(s) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenTyöelämäJaYhteiskuntataitojenOpintojenSuoritus(
    s
  ) ||
  isVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutuksenValinnaistenOpintojenSuoritus(
    s
  )
