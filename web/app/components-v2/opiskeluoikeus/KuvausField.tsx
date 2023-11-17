import React, { useCallback, useState } from 'react'
import { Finnish, isFinnish } from '../../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps } from '../forms/FormField'
import { TestIdLayer, TestIdText, useTestId } from '../../appstate/useTestId'

export type KuvausViewProps = CommonProps<FieldEditorProps<LocalizedString, {}>>

export const KuvausView: React.FC<KuvausViewProps> = (props) => {
  return (
    <TestIdText {...common(props)} id="kuvaus.value">
      {isFinnish(props.value) ? props.value?.fi : props.value?.en || '-'}
    </TestIdText>
  )
}

export type KuvausEditProps = CommonProps<FieldEditorProps<LocalizedString, {}>>

export const KuvausEdit: React.FC<KuvausEditProps> = ({
  onChange,
  initialValue,
  errors,
  ...rest
}) => {
  const testId = useTestId('kuvaus.edit.input')
  const [value, setValue] = useState(initialValue)

  const onChangeCB = useCallback<React.ChangeEventHandler<HTMLTextAreaElement>>(
    (e) => {
      e.preventDefault()
      const fi = e.target.value
      setValue(Finnish({ fi }))
      onChange(Finnish({ fi }))
    },
    [onChange]
  )

  return (
    <div>
      <textarea
        {...common({ ...rest }, ['KuvausEdit'])}
        rows={5}
        cols={40}
        value={isFinnish(value) ? value?.fi : value?.en}
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
