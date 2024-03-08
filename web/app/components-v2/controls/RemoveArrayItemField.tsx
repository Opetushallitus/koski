import * as A from 'fp-ts/Array'
import { pipe } from 'fp-ts/lib/function'
import * as O from 'fp-ts/Option'
import React, { useCallback, useState } from 'react'
import { t } from '../../i18n/i18n'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { CommonProps } from '../CommonProps'
import { FieldEditorProps, FormField } from '../forms/FormField'
import { FormModel, FormOptic } from '../forms/FormModel'
import { FlatButton } from './FlatButton'
import { RaisedButton } from './RaisedButton'

export type ConfirmationTexts = {
  confirm: string
  cancel: string
}

export type RemoveArrayItemFieldProps<
  S extends object,
  A extends any[]
> = CommonProps<{
  form: FormModel<S>
  path: FormOptic<S, A>
  removeAt: number
  label: string | LocalizedString
  onRemove: (index: number) => void
  confirmation?: ConfirmationTexts
}>

export const RemoveArrayItemField = <S extends object, A extends any[]>(
  props: RemoveArrayItemFieldProps<S, A>
): React.ReactElement | null => (
  <FormField
    form={props.form}
    path={props.path}
    view={NullView}
    edit={RemoveArrayItemButton}
    editProps={props}
  />
)

const NullView = () => null

type RemoveArrayItemButtonProps<T extends any[]> = FieldEditorProps<
  T,
  {
    removeAt: number
    label: string | LocalizedString
    onRemove: (index: number) => void
    confirmation?: ConfirmationTexts
    testId?: string
  }
>

const RemoveArrayItemButton = <T extends any[]>(
  props: RemoveArrayItemButtonProps<T>
): React.ReactElement => {
  const [confirmationVisible, setConfirmationVisible] = useState(false)

  const remove = useCallback(() => {
    pipe(
      O.fromNullable(props.value),
      O.chain((as) => A.deleteAt(props.removeAt)(as) as O.Option<T>),
      O.map((as) => {
        props.onChange(as)
        props.onRemove(props.removeAt)
      })
    )
  }, [props])

  return props.confirmation && confirmationVisible ? (
    <>
      <RaisedButton onClick={remove} type="dangerzone" testId="confirm">
        {t(props.confirmation.confirm)}
      </RaisedButton>
      <FlatButton onClick={() => setConfirmationVisible(false)} testId="cancel">
        {t(props.confirmation.cancel)}
      </FlatButton>
    </>
  ) : (
    <FlatButton
      onClick={props.confirmation ? () => setConfirmationVisible(true) : remove}
      testId="button"
    >
      {t(props.label)}
    </FlatButton>
  )
}
