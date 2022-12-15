import {
  VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022,
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
} from './VSTKotoutumiskoulutuksenKieliJaViestintaosaamisenSuoritus2022'
import {
  VSTKotoutumiskoulutuksenOhjauksenSuoritus2022,
  isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022
} from './VSTKotoutumiskoulutuksenOhjauksenSuoritus2022'
import {
  VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022,
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
} from './VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022'
import {
  VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022,
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022
} from './VSTKotoutumiskoulutuksenYhteiskuntaJaTyoelamaosaaminenSuoritus2022'

/**
 * VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022
 *
 * @see `fi.oph.koski.schema.VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022`
 */
export type VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 =
  | VSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022
  | VSTKotoutumiskoulutuksenOhjauksenSuoritus2022
  | VSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022
  | VSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022

export const isVSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 = (
  a: any
): a is VSTKotoutumiskoulutuksenKokonaisuudenOsasuoritus2022 =>
  isVSTKotoutumiskoulutuksenKieliJaViestintäosaamisenSuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenOhjauksenSuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenValinnaistenOpintojenOsasuoritus2022(a) ||
  isVSTKotoutumiskoulutuksenYhteiskuntaJaTyöelämäosaaminenSuoritus2022(a)
