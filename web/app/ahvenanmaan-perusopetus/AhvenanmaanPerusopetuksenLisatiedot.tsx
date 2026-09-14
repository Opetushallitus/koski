import React from 'react'
import { KeyValueTable } from '../components-v2/containers/KeyValueTable'
import { FormModel, getValue } from '../components-v2/forms/FormModel'
import { AikajaksoArrayRow } from '../components-v2/opiskeluoikeus/AikajaksoArrayRow'
import { SingleAikajaksoRow } from '../components-v2/opiskeluoikeus/SingleAikajaksoRow'
import { isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus } from '../types/fi/oph/koski/schema/AhvenanmaanAikuistenPerusopetuksenOppimaaranSuoritus'
import { AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeudenLisatiedot'
import { AhvenanmaanPerusopetuksenOpiskeluoikeus } from '../types/fi/oph/koski/schema/AhvenanmaanPerusopetuksenOpiskeluoikeus'

interface AhvenanmaanPerusopetuksenLisatiedotProps {
  form: FormModel<AhvenanmaanPerusopetuksenOpiskeluoikeus>
}

// Ahvenanmaan perusopetuksen lisätiedoissa ovat kotiopetusjaksot ja muille kuin
// oppivelvollisille alkuvaihe.
export const AhvenanmaanPerusopetuksenLisatiedot: React.FC<
  AhvenanmaanPerusopetuksenLisatiedotProps
> = ({ form }) => {
  const emptyLisatiedot = AhvenanmaanPerusopetuksenOpiskeluoikeudenLisätiedot()
  const lisatiedotPath = form.root.prop('lisätiedot').valueOr(emptyLisatiedot)
  const lisätiedot = getValue(lisatiedotPath)(form.state)
  // Backend hylkää alkuvaiheen oppivelvollisen opiskeluoikeudelta.
  const muuKuinOppivelvollinen = form.state.suoritukset.some(
    isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
  )

  if (!lisätiedot) return null

  return (
    <KeyValueTable>
      {muuKuinOppivelvollinen && (
        <SingleAikajaksoRow
          form={form}
          path={lisatiedotPath.prop('alkuvaihe')}
          label="Alkuvaihe"
          testId="alkuvaihe"
        />
      )}
      <AikajaksoArrayRow
        form={form}
        path={lisatiedotPath.prop('kotiopetusjaksot')}
        label="Kotiopetusjaksot"
        testId="kotiopetusjaksot"
      />
    </KeyValueTable>
  )
}
