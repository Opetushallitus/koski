import React, { useCallback, useContext } from 'react'
import { OpiskeluoikeusContext } from '../../appstate/opiskeluoikeus'
import { classPreferenceName, usePreferences } from '../../appstate/preferences'
import { Laajuus } from '../../types/fi/oph/koski/schema/Laajuus'
import { PaikallinenKoulutusmoduuli } from '../../types/fi/oph/koski/schema/PaikallinenKoulutusmoduuli'
import { StorablePreference } from '../../types/fi/oph/koski/schema/StorablePreference'
import { EmptyObject } from '../../util/objects'
import { CommonProps } from '../CommonProps'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'
import { LaajuusEdit, LaajuusView } from './LaajuusField'

export type PaikallinenKoulutusmoduuliLaajuudella = Extract<
  PaikallinenKoulutusmoduuli,
  { laajuus?: any }
>

export type PaikallisenKoulutusmoduulinLaajuusViewProps<
  T extends PaikallinenKoulutusmoduuliLaajuudella
> = CommonProps<FieldViewerProps<T, EmptyObject>>

export const PaikallisenKoulutusmoduulinLaajuusView = <
  T extends PaikallinenKoulutusmoduuliLaajuudella
>(
  props: PaikallisenKoulutusmoduulinLaajuusViewProps<T>
) => {
  return <LaajuusView value={props.value?.laajuus} />
}

export type PaikallisenKoulutusmoduulinLaajuusEditProps<
  T extends PaikallinenKoulutusmoduuliLaajuudella
> = CommonProps<
  FieldEditorProps<
    T,
    {
      koulutusmoduuli: { className: string }
      template: (p: { arvo: number }) => NonNullable<T['laajuus']>
    }
  >
>

export const PaikallisenKoulutusmoduulinLaajuusEdit = <
  T extends PaikallinenKoulutusmoduuliLaajuudella
>(
  props: PaikallisenKoulutusmoduulinLaajuusEditProps<T>
) => {
  const { organisaatio } = useContext(OpiskeluoikeusContext)
  const preferences = usePreferences(
    organisaatio?.oid,
    classPreferenceName(props.koulutusmoduuli)
  )

  const { value, initialValue, onChange } = props
  const update = useCallback(
    (laajuus?: T['laajuus']) => {
      if (value) {
        const patch = { laajuus } as Partial<StorablePreference>
        onChange({ ...value, ...patch })
        preferences.deferredUpdate(
          value.tunniste.koodiarvo,
          patch,
          initialValue as StorablePreference
        )
      }
    },
    [initialValue, onChange, preferences, value]
  )

  const createLaajuus = (arvo: number) => props.template({ arvo })

  return (
    <LaajuusEdit
      value={props.value?.laajuus}
      onChange={update as (a?: Laajuus) => void}
      createLaajuus={createLaajuus}
    />
  )
}
