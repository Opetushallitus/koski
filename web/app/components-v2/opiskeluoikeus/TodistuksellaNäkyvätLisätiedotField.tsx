import React, { useCallback } from 'react'
import { TestIdText, useTestId } from '../../appstate/useTestId'
import { Finnish, isFinnish } from '../../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { EmptyObject } from '../../util/objects'
import { common, CommonProps } from '../CommonProps'
import { FieldEditorProps } from '../forms/FormField'

export type TodistuksellaNäkyvätLisätiedotViewProps = CommonProps<
  FieldEditorProps<LocalizedString, EmptyObject>
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
  FieldEditorProps<LocalizedString, EmptyObject>
>

export const TodistuksellaNäkyvätLisätiedotEdit: React.FC<
  TodistuksellaNäkyvätLisätiedotEditProps
> = ({ onChange, value, initialValue, ...rest }) => {
  const testId = useTestId('lisätiedot.edit')

  const onChangeCB = useCallback<React.ChangeEventHandler<HTMLTextAreaElement>>(
    (e) => {
      e.preventDefault()
      const fi = e.target.value
      onChange(fi ? Finnish({ fi }) : undefined)
    },
    [onChange]
  )

  return (
    <textarea
      {...common({ ...rest }, ['TodistuksellaNäkyvätLisätiedotEdit'])}
      defaultValue={isFinnish(value) ? value?.fi : value?.en}
      onChange={onChangeCB}
      data-testid={testId}
    />
  )
}
