import { CommonProps } from '../../components-v2/CommonProps'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import { FormModel } from '../../components-v2/forms/FormModel'
import { Koulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Oppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { PaikallinenKoulutusmoduuli } from '../../types/fi/oph/koski/schema/PaikallinenKoulutusmoduuli'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import {
  ArviointiOf,
  KoulutusmoduuliOf,
  OsasuoritusOf
} from '../../util/schema'
import { VapaanSivistystyönKoulutuksenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonKoulutuksenPaatasonSuoritus'
import {
  isVapaanSivistystyönOsaamismerkinSuoritus,
  VapaanSivistystyönOsaamismerkinSuoritus
} from '../../types/fi/oph/koski/schema/VapaanSivistystyonOsaamismerkinSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'

export type VSTPäätasonSuoritusEditorProps<
  T extends PäätasonSuoritusOf<VapaanSivistystyönOpiskeluoikeus>
> = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  oppijaOid: string
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus, T>
  organisaatio?: Koulutustoimija | Oppilaitos
  invalidatable: boolean
  onChangeSuoritus: (suoritusIndex: number) => void
  suorituksenVahvistaminenEiMahdollista: boolean
}>

export type VSTSuoritus =
  | VSTKoulutuksenSuoritus
  | VapaanSivistystyönOsaamismerkinSuoritus

export type VSTKoulutuksenSuoritus =
  | VapaanSivistystyönKoulutuksenPäätasonSuoritus
  | VSTOsasuoritus
  | VSTAlaosasuoritus

export function isVSTKoulutuksenSuoritus(
  x: VSTSuoritus
): x is VSTKoulutuksenSuoritus {
  return !isVapaanSivistystyönOsaamismerkinSuoritus(x)
}

export type VSTKoulutusmoduuli = KoulutusmoduuliOf<VSTSuoritus>
export type VSTKoulutusmoduuliKuvauksella = Extract<
  VSTKoulutusmoduuli,
  { kuvaus: LocalizedString }
>
export type VSTKoulutusmoduuliLaajuudella = Extract<
  VSTKoulutusmoduuli,
  { laajuus?: object }
>

export type VSTOsasuoritus =
  OsasuoritusOf<VapaanSivistystyönKoulutuksenPäätasonSuoritus>

export type VSTAlaosasuoritus = OsasuoritusOf<VSTOsasuoritus>

export type VSTArviointi = ArviointiOf<VSTSuoritus>

export type VSTArviointiPäivällä = Extract<VSTArviointi, { päivä?: any }>

export type VSTSuoritusArvioinnilla = Extract<
  VSTSuoritus,
  { arviointi?: VSTArviointi[] }
>

export type VSTSuoritusOsasuorituksilla = Extract<
  VSTKoulutuksenSuoritus,
  { osasuoritukset?: VSTKoulutuksenSuoritus[] }
>

export type VSTPaikallinenOsasuoritus = Extract<
  VSTKoulutuksenSuoritus,
  { koulutusmoduuli: PaikallinenKoulutusmoduuli }
>

export type VSTSuoritusPaikallisillaOsasuorituksilla = Extract<
  VSTKoulutuksenSuoritus,
  { osasuoritukset?: VSTPaikallinenOsasuoritus[] }
>

export type VSTSuoritusKuvauksella = Extract<
  VSTKoulutuksenSuoritus,
  { koulutusmoduuli: VSTKoulutusmoduuliKuvauksella }
>

export type VSTSuoritusTunnustuksella = Extract<
  VSTKoulutuksenSuoritus,
  { tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen }
>

export type VSTPäätasonSuoritusLaajuudella = Extract<
  VapaanSivistystyönKoulutuksenPäätasonSuoritus,
  { koulutusmoduuli: VSTKoulutusmoduuliLaajuudella }
>

export type VSTPäätasonSuoritusOpintokokonaisuudella = Extract<
  VapaanSivistystyönKoulutuksenPäätasonSuoritus,
  {
    koulutusmoduuli: {
      opintokokonaisuus?: object
    }
  }
>

export type VSTPäätasonSuoritusPerusteella = Extract<
  VapaanSivistystyönKoulutuksenPäätasonSuoritus,
  {
    koulutusmoduuli: {
      perusteenDiaarinumero?: string
    }
  }
>

export const isVSTSuoritusPaikallisillaOsasuorituksilla = (
  s: VSTSuoritusPaikallisillaOsasuorituksilla
): s is VSTSuoritusPaikallisillaOsasuorituksilla =>
  [VapaanSivistystyönJotpaKoulutuksenSuoritus]
    .map((c) => c.className as string)
    .includes(s.$class)

export const nonNullable = <T>(t?: T): T => {
  if (t === undefined) {
    throw new Error('Unexpected undefined value')
  }
  return t
}
