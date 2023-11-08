import React from 'react'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import { KeyValueRow } from '../../components-v2/containers/KeyValueTable'
import { FormField } from '../../components-v2/forms/FormField'
import { FormModel, FormOptic } from '../../components-v2/forms/FormModel'
import {
  LaajuusView,
  laajuusSum
} from '../../components-v2/opiskeluoikeus/LaajuusField'
import {
  OpintokokonaisuusEdit,
  OpintokokonaisuusView
} from '../../components-v2/opiskeluoikeus/OpintokokonaisuusField'
import {
  ToimipisteEdit,
  ToimipisteView
} from '../../components-v2/opiskeluoikeus/OpiskeluoikeudenToimipiste'
import {
  SuorituskieliEdit,
  SuorituskieliView
} from '../../components-v2/opiskeluoikeus/SuorituskieliField'
import {
  TodistuksellaNäkyvätLisätiedotEdit,
  TodistuksellaNäkyvätLisätiedotView
} from '../../components-v2/opiskeluoikeus/TodistuksellaNäkyvätLisätiedotField'
import { InfoLink } from '../../components-v2/texts/InfoLink'
import { Trans } from '../../components-v2/texts/Trans'
import { VapaanSivistystyönOpiskeluoikeus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpiskeluoikeus'
import { VapaanSivistystyönPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonPaatasonSuoritus'
import {
  VSTPäätasonSuoritusLaajuudella,
  VSTPäätasonSuoritusOpintokokonaisuudella,
  VSTPäätasonSuoritusPerusteella
} from './types'
import {
  PerusteEdit,
  PerusteView
} from '../../components-v2/opiskeluoikeus/PerusteField'

export type SuoritusFieldProps<T extends VapaanSivistystyönPäätasonSuoritus> = {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus, T>
}

export const Oppilaitos = <T extends VapaanSivistystyönPäätasonSuoritus>({
  form,
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow
    label="Oppilaitos / toimipiste"
    testId={`${päätasonSuoritus.testId}.toimipiste`}
  >
    <FormField
      form={form}
      path={päätasonSuoritus.path.prop('toimipiste')}
      view={ToimipisteView}
      edit={ToimipisteEdit}
      editProps={{
        onChangeToimipiste: (data: any) => {
          form.updateAt(
            päätasonSuoritus.path.prop('toimipiste').optional(),
            () => data
          )
        }
      }}
    />
  </KeyValueRow>
)

export const Koulutus = <T extends VapaanSivistystyönPäätasonSuoritus>({
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow
    label="Koulutus"
    testId={`${päätasonSuoritus.testId}.koulutusmoduuli.tunniste`}
  >
    <Trans>{päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi}</Trans>
  </KeyValueRow>
)

export const Koulutusmoduuli = <T extends VapaanSivistystyönPäätasonSuoritus>({
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow
    label="Koulutusmoduuli"
    indent={2}
    testId={`${päätasonSuoritus.testId}.koulutusmoduuli.tunniste.koodiarvo`}
  >
    {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}
  </KeyValueRow>
)

export const Peruste = <T extends VSTPäätasonSuoritusPerusteella>({
  form,
  suoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Peruste" indent={2} testId={`${suoritus.testId}.peruste`}>
    <FormField
      form={form}
      path={suoritus.path
        .prop('koulutusmoduuli')
        .prop('perusteenDiaarinumero')
        .optional()}
      view={PerusteView}
      edit={PerusteEdit}
      testId={`${suoritus.testId}.peruste.koulutusmoduuli`}
      editProps={{
        diaariNumero: suoritus.suoritus.tyyppi.koodiarvo
      }}
    />
  </KeyValueRow>
)

export const Opintokokonaisuus = <
  T extends VSTPäätasonSuoritusOpintokokonaisuudella
>({
  form,
  suoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow
    label="Opintokokonaisuus"
    indent={2}
    testId={`${suoritus.testId}.opintokokonaisuus`}
  >
    <FormField
      form={form}
      path={suoritus.path
        .prop('koulutusmoduuli')
        .prop('opintokokonaisuus')
        .optional()}
      view={OpintokokonaisuusView}
      edit={OpintokokonaisuusEdit}
    />
    <InfoLink koulutusmoduuliClass="fi.oph.koski.schema.VapaanSivistystyönJotpaKoulutus" />
  </KeyValueRow>
)

export const Laajuus = <T extends VSTPäätasonSuoritusLaajuudella>({
  form,
  suoritus
}: SuoritusFieldProps<T>) => {
  const castPath = suoritus.path as any as FormOptic<
    VapaanSivistystyönOpiskeluoikeus,
    VSTPäätasonSuoritusLaajuudella
  >
  return (
    <KeyValueRow label="Laajuus" indent={2}>
      <FormField
        form={form}
        path={suoritus.path.prop('koulutusmoduuli').prop('laajuus')}
        view={LaajuusView}
        auto={laajuusSum(
          castPath
            .prop('osasuoritukset')
            .optional()
            .elems()
            .path('koulutusmoduuli.laajuus'),
          form.state
        )}
        testId={`${suoritus.testId}.laajuus`}
      />
    </KeyValueRow>
  )
}

export const Opetuskieli = <T extends VapaanSivistystyönPäätasonSuoritus>({
  form,
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow
    label="Opetuskieli"
    testId={`${päätasonSuoritus.testId}.opetuskieli`}
  >
    <FormField
      form={form}
      path={päätasonSuoritus.path.prop('suorituskieli')}
      view={SuorituskieliView}
      edit={SuorituskieliEdit}
    />
  </KeyValueRow>
)

export const TodistuksenLisätiedot = <
  T extends VapaanSivistystyönPäätasonSuoritus
>({
  form,
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow
    label="Todistuksella näkyvät lisätiedot"
    testId={`${päätasonSuoritus.testId}.todistuksella-nakyvat-lisatiedot`}
  >
    <FormField
      form={form}
      path={päätasonSuoritus.path.prop('todistuksellaNäkyvätLisätiedot')}
      view={TodistuksellaNäkyvätLisätiedotView}
      edit={TodistuksellaNäkyvätLisätiedotEdit}
    />
  </KeyValueRow>
)
