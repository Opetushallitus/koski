import React, { useMemo } from 'react'
import { common, CommonProps } from '../CommonProps'
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
}

export type RadioButtonKey = string

export const RadioButtons = <T,>(
  props: RadioButtonProps<T>
): React.ReactElement | null => {
  const name = useMemo(() => Math.random().toString(), [])

  return props.options ? (
    <ul {...common(props, ['RadioButtons'])}>
      {props.options.map((opt) => {
        const id = `${name}${opt.key}`
        return (
          <li
            key={Math.random()} // TODO: Tässä RadioButtonsin kanssa on nyt joku outo häiriö, eikä sen checked-tila vaihdu ilman tätä. Please PR.
            className="RadioButtons__option"
          >
            <input
              id={id}
              type="radio"
              name={name}
              checked={opt.key === props.value}
              onChange={() => props.onChange(opt.value)}
            />
            <label htmlFor={id} onClick={() => props.onChange(opt.value)}>
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
