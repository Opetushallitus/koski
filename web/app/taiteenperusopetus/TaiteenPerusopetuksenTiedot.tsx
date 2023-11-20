import React, { useMemo } from 'react'
import { ActivePäätasonSuoritus } from '../components-v2/containers/EditorContainer'
import {
  KeyValueRow,
  KeyValueTable
} from '../components-v2/containers/KeyValueTable'
import { FormField } from '../components-v2/forms/FormField'
import { FormModel } from '../components-v2/forms/FormModel'
import {
  laajuusSum,
  LaajuusView
} from '../components-v2/opiskeluoikeus/LaajuusField'
import { Trans } from '../components-v2/texts/Trans'
import { TaiteenPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/TaiteenPerusopetuksenOpiskeluoikeus'
import { TestIdText } from '../appstate/useTestId'

export type TaiteenPerusopetuksenTiedotProps = {
  form: FormModel<TaiteenPerusopetuksenOpiskeluoikeus>
  päätasonSuoritus: ActivePäätasonSuoritus<TaiteenPerusopetuksenOpiskeluoikeus>
}

export const TaiteenPerusopetuksenTiedot: React.FC<
  TaiteenPerusopetuksenTiedotProps
> = ({ form, päätasonSuoritus }) => {
  const opiskeluoikeus = form.state

  const [osasuorituksetPath, opiskeluoikeudenLaajuusPath] = useMemo(
    () => [
      päätasonSuoritus.path.prop('osasuoritukset').optional(),
      päätasonSuoritus.path.prop('koulutusmoduuli').prop('laajuus')
    ],
    [päätasonSuoritus.path]
  )

  const osasuoritustenLaajuudetPath = useMemo(
    () => osasuorituksetPath.elems().path('koulutusmoduuli.laajuus'),
    [osasuorituksetPath]
  )

  const testIdPrefix = `suoritukset.${päätasonSuoritus.index}`

  return (
    <KeyValueTable>
      <KeyValueRow label="Taiteenala">
        <TestIdText id="taiteenala.value">
          <Trans>
            {päätasonSuoritus.suoritus.koulutusmoduuli.taiteenala.nimi}
          </Trans>
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow label="Oppimäärä">
        <TestIdText id="oppimäärä.value">
          <Trans>{opiskeluoikeus.oppimäärä.nimi}</Trans>
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow label="Koulutuksen toteutustapa">
        <TestIdText id="koulutuksenToteutustapa.value">
          <Trans>{form.state.koulutuksenToteutustapa.nimi}</Trans>
        </TestIdText>
      </KeyValueRow>
      {opiskeluoikeus.koulutuksenToteutustapa.koodiarvo ===
        'hankintakoulutus' && (
        <KeyValueRow label="Koulutuksen järjestäjä">
          <TestIdText id="koulutustoimija.value">
            <Trans>{opiskeluoikeus.koulutustoimija?.nimi}</Trans>
          </TestIdText>
        </KeyValueRow>
      )}
      <KeyValueRow label="Oppilaitos">
        <TestIdText id="oppilaitos.value">
          <Trans>{opiskeluoikeus.oppilaitos?.nimi}</Trans>
        </TestIdText>
      </KeyValueRow>
      <KeyValueRow label="Laajuus">
        <FormField
          form={form}
          path={opiskeluoikeudenLaajuusPath}
          view={LaajuusView}
          auto={laajuusSum(osasuoritustenLaajuudetPath, form.state)}
        />
      </KeyValueRow>
    </KeyValueTable>
  )
}
