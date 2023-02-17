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
      <KeyValueRow label="Taiteenala" testId={`${testIdPrefix}.taiteenala`}>
        <Trans>
          {päätasonSuoritus.suoritus.koulutusmoduuli.taiteenala.nimi}
        </Trans>
      </KeyValueRow>
      <KeyValueRow label="Oppimäärä" testId={`${testIdPrefix}.oppimäärä`}>
        <Trans>{opiskeluoikeus.oppimäärä.nimi}</Trans>
      </KeyValueRow>
      <KeyValueRow
        label="Koulutuksen toteutustapa"
        testId={`${testIdPrefix}.koulutuksenToteutustapa`}
      >
        <Trans>{form.state.koulutuksenToteutustapa.nimi}</Trans>
      </KeyValueRow>
      <KeyValueRow label="Oppilaitos" testId={`${testIdPrefix}.oppilaitos`}>
        <Trans>{opiskeluoikeus.oppilaitos?.nimi}</Trans>
      </KeyValueRow>
      <KeyValueRow label="Laajuus" testId={`${testIdPrefix}.laajuus`}>
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
