import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { ArviointiOf, OsasuoritusOf } from '../../util/schema'

export type VSTSuoritus =
  | VapaanSivistystyönPäätasonSuoritus
  | VSTOsasuoritus
  | VSTAlaosasuoritus

export type VSTOsasuoritus = OsasuoritusOf<VapaanSivistystyönPäätasonSuoritus>

export type VSTAlaosasuoritus = OsasuoritusOf<VSTOsasuoritus>

export type VSTArviointi = ArviointiOf<VSTSuoritus>

export type VSTSuoritusArvioinnilla = Extract<
  VSTSuoritus,
  { arviointi?: VSTArviointi[] }
>
