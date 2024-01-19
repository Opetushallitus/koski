import React from 'react'
import { TestIdText } from '../../appstate/useTestId'
import { ActivePäätasonSuoritus } from '../../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../../components-v2/containers/KeyValueTable'
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
  PerusteEdit,
  PerusteView
} from '../../components-v2/opiskeluoikeus/PerusteField'
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
import { VapaanSivistystyönKoulutuksenPäätasonSuoritus } from '../../types/fi/oph/koski/schema/VapaanSivistystyonKoulutuksenPaatasonSuoritus'

export const PäätasosuorituksenTiedot: React.FC<{
  children: React.ReactNode
}> = (props) => <KeyValueTable>{props.children}</KeyValueTable>

export type SuoritusFieldProps<T extends VapaanSivistystyönPäätasonSuoritus> = {
  form: FormModel<VapaanSivistystyönOpiskeluoikeus>
  suoritus: ActivePäätasonSuoritus<VapaanSivistystyönOpiskeluoikeus, T>
}

export const Oppilaitos = <T extends VapaanSivistystyönPäätasonSuoritus>({
  form,
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Oppilaitos / toimipiste">
    <FormField
      form={form}
      path={päätasonSuoritus.path.prop('toimipiste')}
      view={ToimipisteView}
      edit={ToimipisteEdit}
      editProps={{
        onChangeToimipiste: (data: any) => {
          form.updateAt(päätasonSuoritus.path.prop('toimipiste'), () => data)
        }
      }}
    />
  </KeyValueRow>
)

export const Koulutus = <T extends VapaanSivistystyönPäätasonSuoritus>({
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Koulutus">
    <TestIdText id="tunniste.nimi">
      <Trans>{päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.nimi}</Trans>
    </TestIdText>
  </KeyValueRow>
)

export const Koulutusmoduuli = <T extends VapaanSivistystyönPäätasonSuoritus>({
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Koulutusmoduuli">
    <TestIdText id="tunniste.koodiarvo">
      {päätasonSuoritus.suoritus.koulutusmoduuli.tunniste.koodiarvo}
    </TestIdText>
  </KeyValueRow>
)

export const Peruste = <T extends VSTPäätasonSuoritusPerusteella>({
  form,
  suoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Peruste">
    <FormField
      form={form}
      path={suoritus.path.prop('koulutusmoduuli').prop('perusteenDiaarinumero')}
      view={PerusteView}
      edit={PerusteEdit}
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
  <KeyValueRow label="Opintokokonaisuus">
    <FormField
      form={form}
      path={suoritus.path.prop('koulutusmoduuli').prop('opintokokonaisuus')}
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
    <KeyValueRow label="Laajuus">
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
      />
    </KeyValueRow>
  )
}

export const Opetuskieli = <
  T extends VapaanSivistystyönKoulutuksenPäätasonSuoritus
>({
  form,
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Opetuskieli">
    <FormField
      form={form}
      path={päätasonSuoritus.path.prop('suorituskieli')}
      view={SuorituskieliView}
      edit={SuorituskieliEdit}
    />
  </KeyValueRow>
)

export const TodistuksenLisätiedot = <
  T extends VapaanSivistystyönKoulutuksenPäätasonSuoritus
>({
  form,
  suoritus: päätasonSuoritus
}: SuoritusFieldProps<T>) => (
  <KeyValueRow label="Todistuksella näkyvät lisätiedot">
    <FormField
      form={form}
      path={päätasonSuoritus.path.prop('todistuksellaNäkyvätLisätiedot')}
      view={TodistuksellaNäkyvätLisätiedotView}
      edit={TodistuksellaNäkyvätLisätiedotEdit}
    />
  </KeyValueRow>
)
