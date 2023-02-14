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
import { t } from '../i18n/i18n'
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

  return (
    <KeyValueTable>
      <KeyValueRow name="Taiteenala">
        <Trans>
          {päätasonSuoritus.suoritus.koulutusmoduuli.taiteenala.nimi}
        </Trans>
      </KeyValueRow>
      <KeyValueRow name="Oppimäärä">
        <Trans>{opiskeluoikeus.oppimäärä.nimi}</Trans>
      </KeyValueRow>
      <KeyValueRow name="Koulutuksen toteutustapa">
        <Trans>{form.state.koulutuksenToteutustapa.nimi}</Trans>
      </KeyValueRow>
      <KeyValueRow name="Oppilaitos">
        <Trans>{opiskeluoikeus.oppilaitos?.nimi}</Trans>
      </KeyValueRow>
      <KeyValueRow name="Laajuus">
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
