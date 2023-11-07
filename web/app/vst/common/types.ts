import { CommonProps } from '../../components-v2/CommonProps'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import { Koulutustoimija } from '../../types/fi/oph/koski/schema/Koulutustoimija'
import { Oppilaitos } from '../../types/fi/oph/koski/schema/Oppilaitos'
import { PaikallinenKoulutusmoduuli } from '../../types/fi/oph/koski/schema/PaikallinenKoulutusmoduuli'
import { VapaanSivistystyönJotpaKoulutuksenSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonJotpaKoulutuksenSuoritus'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import { PäätasonSuoritusOf } from '../../util/opiskeluoikeus'
import { ArviointiOf, OsasuoritusOf } from '../../util/schema'

export type VSTPäätasonSuoritusEditorProps<
  T extends PäätasonSuoritusOf<VapaanSivistystyönOpiskeluoikeus>
> = CommonProps<{
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  oppijaOid: string
  päätasonSuoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus, T>
  organisaatio?: Koulutustoimija | Oppilaitos
  invalidatable: boolean
  onChangeSuoritus: (suoritusIndex: number) => void
  onCreateOsasuoritus: (
    suoritusPath: FormOptic<VapaanSivistystyönPäätasonSuoritus, any>,
    osasuoritus: VSTSuoritus
  ) => void // TODO TOR-2086: Tyypitys
  suoritusVahvistettu: boolean
}>

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

export type VSTPäätasonSuoritusLaajuudella = Extract<
  VapaanSivistystyönPäätasonSuoritus,
  {
    koulutusmoduuli: {
      laajuus?: object
    }
  }
>

export type VSTPäätasonSuoritusOpintokokonaisuudella = Extract<
  VapaanSivistystyönPäätasonSuoritus,
  {
    koulutusmoduuli: {
      opintokokonaisuus?: object
    }
  }
>

export const isVSTSuoritusPaikallisillaOsasuorituksilla = (
  s: VSTSuoritusPaikallisillaOsasuorituksilla
): s is VSTSuoritusPaikallisillaOsasuorituksilla =>
  [VapaanSivistystyönJotpaKoulutuksenSuoritus]
    .map((c) => c.className as string)
    .includes(s.$class)
