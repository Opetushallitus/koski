import * as $ from 'optics-ts'
import React from 'react'
import { TestIdLayer, useTestId } from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { SelitettyOsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/SelitettyOsaamisenTunnustaminen'
import { VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/VapaanSivistystyonOpintojenSuorituksenOsaamisenTunnustaminen'
import { EmptyObject } from '../../util/objects'
import { allLanguages } from '../../util/optics'
import { CommonProps, common } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { Removable } from '../controls/Removable'
import { MultilineTextEdit } from '../controls/TextField'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

export type TunnustusViewProps<T extends SelitettyOsaamisenTunnustaminen> =
  CommonProps<FieldViewerProps<T, EmptyObject>>

export const TunnustusView = <T extends SelitettyOsaamisenTunnustaminen>(
  props: TunnustusViewProps<T>
): React.ReactElement | null => {
  const testId = useTestId('tunnustettu.value')

  return (
    <div {...common(props, ['TunnustusView'])} data-testid={testId}>
      {t(props.value?.selite) || '–'}
    </div>
  )
}

export type TunnustusEditProps<
  T extends
    | SelitettyOsaamisenTunnustaminen
    | VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
> = FieldEditorProps<
  T,
  {
    createEmptyTunnustus: () => T
  }
>

export const TunnustusEdit = <
  T extends
    | SelitettyOsaamisenTunnustaminen
    | VapaanSivistystyönOpintojenSuorituksenOsaamisenTunnustaminen
>(
  props: TunnustusEditProps<T>
): React.ReactElement | null => {
  const selitePath = $.optic_<T | undefined>()
    .optional()
    .prop('selite')
    .compose(allLanguages)

  const value = t(props.value?.selite)
  const onChange = (s?: string) =>
    props.onChange($.set(selitePath)(s)(props.value) as T)

  const add = () => props.onChange(props.createEmptyTunnustus())
  const remove = () => props.onChange(undefined)

  return (
    <div {...common(props, ['TunnustusEdit'])}>
      <TestIdLayer id="tunnustettu.edit">
        {props.value === undefined ? (
          <FlatButton onClick={add} testId="add">
            {t('lisää')}
          </FlatButton>
        ) : (
          <Removable onClick={remove}>
            <MultilineTextEdit
              value={value}
              onChange={onChange}
              placeholder="Selite"
              testId="selite"
            />
          </Removable>
        )}
        <FieldErrors errors={props.errors} />
      </TestIdLayer>
    </div>
  )
}
