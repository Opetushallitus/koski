import { CommonProps } from '../../components-v2/CommonProps'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { Koulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { Oppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { PaikallinenKoulutusmoduuli } from '../../types/fi/oph/koski/schema/PaikallinenKoulutusmoduuli'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import {
  ArviointiOf,
  KoulutusmoduuliOf,
  OsasuoritusOf
} from '../../util/schema'

export type VSTPäätasonSuoritusEditorProps<
  T extends PäätasonSuoritusOf<VapaanSivistystyönOpiskeluoikeus>
> = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  oppijaOid: string
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus, T>
  organisaatio?: Koulutustoimija | Oppilaitos
  invalidatable: boolean
  onChangeSuoritus: (suoritusIndex: number) => void
  suoritusVahvistettu: boolean
}>

export type VSTSuoritus =
  | VapaanSivistystyönPäätasonSuoritus
  | VSTOsasuoritus
  | VSTAlaosasuoritus

export type VSTKoulutusmoduuli = KoulutusmoduuliOf<VSTSuoritus>
export type VSTKoulutusmoduuliKuvauksella = Extract<
  VSTKoulutusmoduuli,
  { kuvaus: LocalizedString }
>
export type VSTKoulutusmoduuliLaajuudella = Extract<
  VSTKoulutusmoduuli,
  { laajuus?: object }
>

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

export type VSTSuoritusKuvauksella = Extract<
  VSTSuoritus,
  { koulutusmoduuli: VSTKoulutusmoduuliKuvauksella }
>

export type VSTSuoritusTunnustuksella = Extract<
  VSTSuoritus,
  { tunnustettu?: VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen }
>

export type VSTPäätasonSuoritusLaajuudella = Extract<
  VapaanSivistystyönPäätasonSuoritus,
  { koulutusmoduuli: VSTKoulutusmoduuliLaajuudella }
>

export type VSTPäätasonSuoritusOpintokokonaisuudella = Extract<
  VapaanSivistystyönPäätasonSuoritus,
  {
    koulutusmoduuli: {
      opintokokonaisuus?: object
    }
  }
>

export type VSTPäätasonSuoritusPerusteella = Extract<
  VapaanSivistystyönPäätasonSuoritus,
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
