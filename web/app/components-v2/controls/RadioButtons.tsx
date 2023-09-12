import React from 'react'
import { common, CommonProps, cx, testId } from '../CommonProps'
import { FieldEditorProps } from '../forms/FormField'

export type RadioButtonProps<T> = CommonProps<{
  options?: Array<RadioButtonOption<T>>
  value?: RadioButtonKey
  onChange: (value: T) => void
}>

export type RadioButtonOption<T> = {
  key: RadioButtonKey
  label: React.ReactNode
  value: T
  disabled?: boolean
}

export type RadioButtonKey = string

export const RadioButtons = <T,>(
  props: RadioButtonProps<T>
): React.ReactElement | null => {
  return props.options ? (
    <ul {...common(props, ['RadioButtons'])}>
      {props.options.map((opt, i) => {
        const id = `RadioButton_${i}_${opt.key}`
        return (
          <li
            key={id}
            className={cx(
              'RadioButtons__option',
              opt.disabled && 'RadioButtons__option--disabled'
            )}
          >
            <input
              id={id}
              type="radio"
              name={id}
              checked={opt.key === props.value}
              onChange={() => props.onChange(opt.value)}
              disabled={opt.disabled}
              {...testId(props, `options.${opt.key}`)}
            />
            <label
              htmlFor={id}
              onClick={
                !opt.disabled ? () => props.onChange(opt.value) : undefined
              }
            >
              {opt.label}
            </label>
          </li>
        )
      })}
    </ul>
  ) : null
}

export type RadioButtonsEditProps<T> = CommonProps<
  FieldEditorProps<
    T,
    {
      getKey: (value: T) => string
      options?: Array<RadioButtonOption<T>>
    }
  >
>

export const RadioButtonsEdit = <T,>(
  props: RadioButtonsEditProps<T>
): React.ReactElement => {
  const { value, ...rest } = props
  const valueKey = props.value && props.getKey(props.value)
  return <RadioButtons {...rest} value={valueKey} options={props.options} />
}
