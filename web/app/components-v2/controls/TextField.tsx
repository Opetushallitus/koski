import * as A from 'fp-ts/Array'
import React, { useCallback } from 'react'
import { t } from '../../i18n/i18n'
import { common, CommonProps, cx } from '../CommonProps'
import { FieldErrors } from '../forms/FieldErrors'
import { FieldEditBaseProps, FieldViewBaseProps } from '../forms/FormField'

export type TextViewProps = CommonProps<FieldViewBaseProps<string>>

export const TextView: React.FC<TextViewProps> = (props) => (
  <div {...common(props, ['TextView'])}>{props.value}</div>
)

export type TextEditProps = CommonProps<
  FieldEditBaseProps<string> & {
    placeholder?: string
    autoFocus?: boolean
    allowEmpty?: boolean
  }
>

export const TextEdit: React.FC<TextEditProps> = (props) => {
  const onChange: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      props.onChange(event.target.value)
    },
    [props.onChange]
  )

  const requiredButEmpty = Boolean(!props.allowEmpty && !props.value)

  return (
    <label {...common(props)}>
      <input
        className={cx(
          'TextEdit__input',
          (requiredButEmpty || A.isNonEmpty(props.errors)) &&
            'TextEdit__input--error'
        )}
        placeholder={props.placeholder}
        value={props.value}
        onChange={onChange}
        autoFocus={props.autoFocus}
      />
      <FieldErrors
        errors={props.errors}
        customError={
          requiredButEmpty ? t('Kenttä ei voi olla tyhjä') : undefined
        }
      />
    </label>
  )
}
