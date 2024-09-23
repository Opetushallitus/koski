import React, { useCallback, useContext } from 'react'
import { OpiskeluoikeusContext } from '../../appstate/opiskeluoikeus'
import { classPreferenceName, usePreferences } from '../../appstate/preferences'
import { TestIdLayer, TestIdText, useTestId } from '../../appstate/useTestId'
import { finnish, t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { PaikallinenKoulutusmoduuli } from '../../types/fi/oph/koski/schema/PaikallinenKoulutusmoduuli'
import { StorablePreference } from '../../types/fi/oph/koski/schema/StorablePreference'
import { CommonProps, common } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps } from '../forms/FormField'
import { EmptyObject } from '../../util/objects'

export type KuvailtuPaikallinenKoulutusmoduuli = Extract<
  PaikallinenKoulutusmoduuli,
  { kuvaus?: LocalizedString }
>

export type KuvausViewProps<T extends KuvailtuPaikallinenKoulutusmoduuli> =
  CommonProps<FieldEditorProps<T, EmptyObject>>

export const KuvausView = <T extends KuvailtuPaikallinenKoulutusmoduuli>(
  props: KuvausViewProps<T>
) => (
  <TestIdText {...common(props)} id="kuvaus.value">
    {props.value?.kuvaus ? t(props.value.kuvaus) : '-'}
  </TestIdText>
)

export type KuvausEditProps<T extends KuvailtuPaikallinenKoulutusmoduuli> =
  CommonProps<FieldEditorProps<T, EmptyObject>>

export const KuvausEdit = <T extends KuvailtuPaikallinenKoulutusmoduuli>({
  onChange,
  initialValue,
  value,
  errors,
  ...rest
}: KuvausEditProps<T>) => {
  const testId = useTestId('kuvaus.edit.input')

  const { organisaatio } = useContext(OpiskeluoikeusContext)
  const preferences = usePreferences(
    organisaatio?.oid,
    value && classPreferenceName(value)
  )

  const onChangeCB = useCallback<React.ChangeEventHandler<HTMLTextAreaElement>>(
    (e) => {
      e.preventDefault()
      const patch = { kuvaus: finnish(e.target.value) }
      const newValue = { ...value, ...patch } as T
      onChange(newValue)

      if (value?.tunniste.koodiarvo && initialValue) {
        preferences.deferredUpdate(
          value.tunniste.koodiarvo,
          patch,
          initialValue as StorablePreference
        )
      }
    },
    [initialValue, onChange, preferences, value]
  )

  return (
    <div>
      <textarea
        {...common({ ...rest }, ['KuvausEdit'])}
        rows={5}
        cols={40}
        defaultValue={t(value?.kuvaus)}
        onChange={onChangeCB}
        data-testid={testId}
      />
      {errors && (
        <TestIdLayer id="kuvaus.edit">
          <FieldErrors errors={errors} />
        </TestIdLayer>
      )}
    </div>
  )
}
