import React, { useCallback, useState } from 'react'
import { Finnish, isFinnish } from '../../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { FieldEditorProps } from '../forms/FormField'
import { TestIdText, useTestId } from '../../appstate/useTestId'

export type TodistuksellaNäkyvätLisätiedotViewProps = CommonProps<
  FieldEditorProps<LocalizedString, {}>
>

export const TodistuksellaNäkyvätLisätiedotView: React.FC<
  TodistuksellaNäkyvätLisätiedotViewProps
> = ({ value }) => {
  return (
    <TestIdText id="lisätiedot.value">
      {isFinnish(value) ? value?.fi : value?.en || '-'}
    </TestIdText>
  )
}

export type TodistuksellaNäkyvätLisätiedotEditProps = CommonProps<
  FieldEditorProps<LocalizedString, {}>
>

export const TodistuksellaNäkyvätLisätiedotEdit: React.FC<
  TodistuksellaNäkyvätLisätiedotEditProps
> = ({ onChange, initialValue, ...rest }) => {
  const [value, setValue] = useState(initialValue)
  const testId = useTestId('lisätiedot.edit')

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
    <textarea
      {...common({ ...rest }, ['TodistuksellaNäkyvätLisätiedotEdit'])}
      value={isFinnish(value) ? value?.fi : value?.en}
      onChange={onChangeCB}
      data-testid={testId}
    />
  )
}
