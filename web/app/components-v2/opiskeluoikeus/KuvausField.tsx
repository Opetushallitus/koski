import React, { useCallback, useState } from 'react'
import { Finnish, isFinnish } from '../../types/fi/oph/koski/schema/Finnish'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { common, CommonProps } from '../CommonProps'
import { FieldEditorProps } from '../forms/FormField'

export type KuvausViewProps = CommonProps<FieldEditorProps<LocalizedString, {}>>

export const KuvausView: React.FC<KuvausViewProps> = ({ value }) => {
  return <span>{isFinnish(value) ? value?.fi : value?.en || '-'}</span>
}

export type KuvausEditProps = CommonProps<FieldEditorProps<LocalizedString, {}>>

export const KuvausEdit: React.FC<KuvausEditProps> = ({
  onChange,
  initialValue,
  ...rest
}) => {
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
    <textarea
      {...common({ ...rest }, ['KuvausEdit'])}
      rows={5}
      cols={40}
      value={isFinnish(value) ? value?.fi : value?.en}
      onChange={onChangeCB}
    />
  )
}
