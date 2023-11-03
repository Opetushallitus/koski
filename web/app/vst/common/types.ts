import { PaikallinenKoulutusmoduuli } from '../../types/fi/oph/koski/schema/PaikallinenKoulutusmoduuli'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
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

export type VSTSuoritusOsasuorituksilla = Extract<
  VSTSuoritus,
  { osasuoritukset?: VSTSuoritus[] }
>

export type VSTPaikallinenOsasuoritus = Extract<
  VSTSuoritus,
  { koulutusmoduuli: PaikallinenKoulutusmoduuli }
>

export type VSTSuoritusPaikallisillaOsasuorituksilla = Extract<
  VSTSuoritus,
  { osasuoritukset?: VSTPaikallinenOsasuoritus[] }
>

export const isVSTSuoritusPaikallisillaOsasuorituksilla = (
  s: VSTSuoritusPaikallisillaOsasuorituksilla
): s is VSTSuoritusPaikallisillaOsasuorituksilla =>
  [VapaanSivistystyönJotpaKoulutuksenSuoritus]
    .map((c) => c.className as string)
    .includes(s.$class)
