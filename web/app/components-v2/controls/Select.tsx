import { flow } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import React, { useCallback, useEffect, useRef, useState } from 'react'
import { KoodistokoodiviiteKoodistonNimellä } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import {
  flattenObj,
  isSingularObject,
  mapRecordToArray,
  mapRecordValues,
  pluck
} from '../../util/fp/objects'
import { clamp } from '../../util/numbers'
import { baseProps, BaseProps } from '../baseProps'

export type SelectProps<T> = BaseProps & {
  initialValue?: OptionKey
  value?: OptionKey
  options: GroupedOptions<T>
  onChange: (option?: SelectOption<T>) => void
  placeholder?: string | LocalizedString
  emptyOption?: string | LocalizedString
}

export type GroupedOptions<T> = Record<OptionGroupName, Array<SelectOption<T>>>
export type OptionGroupName = string
export type OptionKey = string

export type SelectOption<T> = {
  key: OptionKey
  label: string
  display?: React.ReactNode
  value: T
}

export const Select = <T,>(props: SelectProps<T>) => {
  const [dropdownVisible, setDropdownVisible] = useState(false)
  const [displayValue, setDisplayValue] = useState<string>('')
  const [hoveredOption, setHoveredOption] = useState<
    SelectOption<T> | undefined
  >()
  const selectContainer = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const option =
      props.value &&
      flattenObj(props.options).find((o) => o.key === props.value)
    setDisplayValue(option ? option.label : '')
  }, [props.value, props.options])

  const onFocus = useCallback(() => {
    setDropdownVisible(true)
  }, [])

  // Losing the focus

  const onBlur: React.FocusEventHandler = useCallback((event) => {
    const currentTarget = event.currentTarget
    setTimeout(() => {
      if (document.activeElement?.contains(currentTarget)) {
        setDropdownVisible(false)
      }
    }, 250) // TODO: Tää on vähän vaarallinen, voi aiheuttaa flakya
  }, [])

  useEffect(() => {
    const mouseHandler = (event: MouseEvent) => {
      return setDropdownVisible(
        (event.target instanceof Element &&
          selectContainer.current?.contains(event.target)) ||
          false
      )
    }
    document.body.addEventListener('click', mouseHandler)
    return () => {
      document.body.removeEventListener('click', mouseHandler)
    }
  }, [])

  // Changes

  const onChange = useCallback(
    (option: SelectOption<T>) => {
      setDisplayValue(option.label)
      setDropdownVisible(false)
      props.onChange(option)
    },
    [props.onChange]
  )

  // Interaction

  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      switch (event.key) {
        case 'ArrowDown':
          setHoveredOption(selectOption(props.options, hoveredOption, 1))
          setDropdownVisible(true)
          event.preventDefault()
          event.stopPropagation()
          return
        case 'ArrowUp':
          setHoveredOption(selectOption(props.options, hoveredOption, -1))
          setDropdownVisible(true)
          event.preventDefault()
          event.stopPropagation()
          return
        case 'Escape':
          setDropdownVisible(false)
          event.preventDefault()
          event.stopPropagation()
          return
        case 'Enter':
          setDropdownVisible(false)
          event.preventDefault()
          event.stopPropagation()
          if (hoveredOption && dropdownVisible) {
            onChange(hoveredOption)
          }
          return
        default:
        // console.log(event.key)
      }
    },
    [props.options, hoveredOption, dropdownVisible]
  )

  return (
    <div
      {...baseProps(props, 'Select')}
      onClick={onFocus}
      onTouchStart={onFocus}
      onKeyDown={onKeyDown}
      onBlur={onBlur}
      ref={selectContainer}
    >
      <input
        className="Select__input"
        placeholder={t(props.placeholder)}
        value={displayValue}
        onChange={doNothing} // TODO: Tähän vois hakufiltteröintihomman toteuttaa
        // onKeyDown={onKeyDown}
      />
      {dropdownVisible && (
        <div className="Select__optionListContainer">
          <ul className="Select__optionList">
            {isSingularObject(props.options) ? (
              <Options
                options={flattenObj(props.options)}
                hoveredOption={hoveredOption}
                onClick={onChange}
                onMouseOver={setHoveredOption}
              />
            ) : (
              mapRecordToArray(
                (options: Array<SelectOption<T>>, group: OptionGroupName) => (
                  <optgroup key={group} label={group}>
                    <Options
                      options={options}
                      hoveredOption={hoveredOption}
                      onClick={onChange}
                      onMouseOver={setHoveredOption}
                    />
                  </optgroup>
                )
              )(props.options)
            )}
          </ul>
        </div>
      )}
    </div>
  )
}

const selectOption = <T,>(
  options: GroupedOptions<T>,
  current: SelectOption<T> | undefined,
  steps: number
): SelectOption<T> => {
  const flatOptions = flattenObj(options)
  const currentIndex = current
    ? flatOptions?.findIndex((o) => o.key === current.key)
    : -1
  const index = clamp(0, flatOptions.length - 1)(currentIndex + steps)
  return flatOptions[index]
}

type OptionsProps<T> = {
  options: Array<SelectOption<T>>
  hoveredOption?: SelectOption<T>
  onClick: (o: SelectOption<T>) => void
  onMouseOver: (o: SelectOption<T>) => void
}

const Options = <T,>(props: OptionsProps<T>) => {
  const onClick = (option: SelectOption<T>) => (event: React.MouseEvent) => {
    event.preventDefault()
    event.stopPropagation()
    props.onClick(option)
  }

  return (
    <>
      {props.options.map((opt) => (
        <li
          {...baseProps(
            'Select__option',
            props.hoveredOption?.key === opt.key && 'Select__option--hover'
          )}
          key={opt.key}
          onClick={onClick(opt)}
          onMouseOver={() => props.onMouseOver(opt)}
        >
          {opt.display || t(opt.label)}
        </li>
      ))}
    </>
  )
}

export const groupKoodistoToOptions: <T extends string>(
  koodit: KoodistokoodiviiteKoodistonNimellä<T>[]
) => GroupedOptions<Koodistokoodiviite<T>> = flow(
  NEA.groupBy(pluck('koodistoNimi')),
  mapRecordValues(
    NEA.map((k) => ({
      key: k.id,
      label: t(k.koodiviite.nimi) || k.koodiviite.koodiarvo,
      value: k.koodiviite
    }))
  )
)

const doNothing = () => {}
