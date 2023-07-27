import * as $ from 'optics-ts'
import React from 'react'
import { localize, t } from '../../i18n/i18n'
import { OsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/OsaamisenTunnustaminen'
import { SelitettyOsaamisenTunnustaminen } from '../../types/fi/oph/koski/schema/SelitettyOsaamisenTunnustaminen'
import { TaiteenPerusopetuksenOsasuorituksenTunnustus } from '../../types/fi/oph/koski/schema/TaiteenPerusopetuksenOsasuorituksenTunnustus'
import { allLanguages } from '../../util/optics'
import { assertNever } from '../../util/selfcare'
import { ClassOf } from '../../util/types'
import { common, CommonProps, testId } from '../CommonProps'
import { FlatButton } from '../controls/FlatButton'
import { Removable } from '../controls/Removable'
import { MultilineTextEdit } from '../controls/TextField'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditorProps, FieldViewerProps } from '../forms/FormField'

export type TunnustusViewProps<T extends SelitettyOsaamisenTunnustaminen> =
  // eslint-disable-next-line @typescript-eslint/ban-types
  CommonProps<FieldViewerProps<T, {}>>

export const TunnustusView = <T extends SelitettyOsaamisenTunnustaminen>(
  props: TunnustusViewProps<T>
): React.ReactElement | null => {
  return (
    <div {...common(props, ['TunnustusView'])} {...testId(props)}>
      {t(props.value?.selite) || '–'}
    </div>
  )
}

export type TunnustusEditProps<T extends SelitettyOsaamisenTunnustaminen> =
  FieldEditorProps<
    T,
    {
      tunnustusClass: ClassOf<T>
    }
  >

export const TunnustusEdit = <T extends SelitettyOsaamisenTunnustaminen>(
  props: TunnustusEditProps<T>
): React.ReactElement | null => {
  const selitePath = $.optic_<T | undefined>()
    .optional()
    .prop('selite')
    .compose(allLanguages)

  const value = t(props.value?.selite)
  const onChange = (s?: string) =>
    props.onChange($.set(selitePath)(s)(props.value) as T)

  const add = () =>
    props.onChange(createEmptyTunnustus(props.tunnustusClass) as any)
  const remove = () => props.onChange(undefined)

  return (
    <div {...common(props, ['TunnustusEdit'])}>
      {props.value === undefined ? (
        <FlatButton onClick={add}>{'lisää'}</FlatButton>
      ) : (
        <Removable onClick={remove} testId={props.testId}>
          <MultilineTextEdit
            value={value}
            onChange={onChange}
            placeholder="Selite"
            testId={props.testId}
          />
        </Removable>
      )}
      <FieldErrors errors={props.errors} />
    </div>
  )
}

const createEmptyTunnustus = <T extends SelitettyOsaamisenTunnustaminen>(
  className: ClassOf<T>
): T => {
  const selite = localize('')
  switch (className) {
    case OsaamisenTunnustaminen.className:
      return OsaamisenTunnustaminen({ selite }) as T
    case TaiteenPerusopetuksenOsasuorituksenTunnustus.className:
      return TaiteenPerusopetuksenOsasuorituksenTunnustus({ selite }) as T
    default:
      return assertNever(className)
  }
}
