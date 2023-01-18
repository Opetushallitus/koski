import { flow } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
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
import { cx, CommonProps, common } from '../CommonProps'

export type SelectProps<T> = CommonProps<{
  initialValue?: OptionKey
  value?: OptionKey
  options: GroupedOptions<T>
  onChange: (option?: SelectOption<T>) => void
  placeholder?: string | LocalizedString
  hideEmpty?: boolean
}>

export type GroupedOptions<T> = Record<OptionGroupName, Array<SelectOption<T>>>
export type OptionGroupName = string
export type OptionKey = string

export type SelectOption<T> = {
  key: OptionKey
  label: string
  display?: React.ReactNode
  value: T
  ignoreFilter?: boolean
}

const scrollHoveredIntoView = (
  selectContainer: React.RefObject<HTMLDivElement>
) => {
  setTimeout(() => {
    selectContainer.current
      ?.querySelector('.Select__option--hover')
      ?.scrollIntoView({ block: 'nearest' })
  }, 0)
}

export const Select = <T,>(props: SelectProps<T>) => {
  const [dropdownVisible, setDropdownVisible] = useState(false)
  const [displayValue, setDisplayValue] = useState<string>('')
  const [hoveredOption, setHoveredOption] = useState<
    SelectOption<T> | undefined
  >()
  const [filter, setFilter] = useState<string | null>(null)
  const selectContainer = useRef<HTMLDivElement>(null)

  useEffect(() => {
    const option =
      props.value &&
      flattenObj(props.options).find((o) => o.key === props.value)
    setDisplayValue(option ? option.label : '')
  }, [props.value, props.options])

  useEffect(() => {
    if (props.hideEmpty) {
      setHoveredOption(flattenObj(props.options)[0])
    }
  }, [])

  const onFocus = useCallback(() => {
    setDropdownVisible(true)
  }, [])

  // Losing the focus

  const onBlur: React.FocusEventHandler = useCallback((event) => {
    setTimeout(() => {
      setDropdownVisible(false)
    }, 1000) // TODO: Tää on vähän vaarallinen, voi aiheuttaa flakya
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
    (option?: SelectOption<T>) => {
      setDropdownVisible(false)
      setFilter(null)
      props.onChange(option)
    },
    [props.onChange]
  )

  // Filter options

  const options: GroupedOptions<T> = useMemo(() => {
    if (filter === '' || filter === null) return props.options
    const needle = filter.toLowerCase()
    return mapRecordValues((os: Array<SelectOption<T>>) =>
      os.filter((o) => o.ignoreFilter || o.label.toLowerCase().includes(needle))
    )(props.options)
  }, [filter, props.options])

  // Interaction

  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      switch (event.key) {
        case 'Tab':
          setDropdownVisible(false)
          return
        case 'ArrowDown':
          if (dropdownVisible) {
            setHoveredOption(selectOption(options, hoveredOption, 1))
          }
          setDropdownVisible(true)
          event.preventDefault()
          event.stopPropagation()
          scrollHoveredIntoView(selectContainer)
          return
        case 'ArrowUp':
          if (dropdownVisible) {
            setHoveredOption(selectOption(options, hoveredOption, -1))
          }
          setDropdownVisible(true)
          event.preventDefault()
          event.stopPropagation()
          scrollHoveredIntoView(selectContainer)
          return
        case 'Escape':
          setDropdownVisible(false)
          setFilter(null)
          event.preventDefault()
          event.stopPropagation()
          return
        case 'Enter':
          setDropdownVisible(false)
          event.preventDefault()
          event.stopPropagation()
          if (dropdownVisible) {
            onChange(hoveredOption)
          }
          return
        default:
        // console.log(event.key)
      }
    },
    [options, hoveredOption, dropdownVisible]
  )

  const onUserType: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      setFilter(event.target.value)
      setDropdownVisible(true)
      const needle = event.target.value.toLowerCase()
      if (needle && !props.hideEmpty) {
        const firstMatch = flattenObj(props.options).find((o) =>
          o.label.toLowerCase().includes(needle)
        )
        setHoveredOption(firstMatch)
      } else {
        setHoveredOption(undefined)
      }
    },
    [props.options, props.hideEmpty]
  )

  // Render

  return (
    <div
      {...common(props, ['Select'])}
      onClick={onFocus}
      onTouchStart={onFocus}
      onKeyDown={onKeyDown}
      onBlur={onBlur}
      ref={selectContainer}
    >
      <input
        className="Select__input"
        placeholder={t(props.placeholder)}
        value={filter === null ? displayValue : filter}
        onChange={onUserType}
        type="search"
        autoComplete="off"
      />
      {dropdownVisible && (
        <div className="Select__optionListContainer">
          <ul className="Select__optionList">
            {!props.hideEmpty && !filter && (
              <li
                className={cx(
                  props.className,
                  'Select__option',
                  !hoveredOption && 'Select__option--hover'
                )}
                onClick={() => onChange(undefined)}
                onMouseOver={() => setHoveredOption(undefined)}
              >
                {t('Ei valintaa')}
              </li>
            )}

            {isSingularObject(options) ? (
              <Options
                options={flattenObj(options)}
                hoveredOption={hoveredOption}
                onClick={onChange}
                onMouseOver={setHoveredOption}
              />
            ) : (
              mapRecordToArray(
                (options: Array<SelectOption<T>>, group: OptionGroupName) => (
                  // TODO: Renderöi groupin nimi
                  <Options
                    options={options}
                    hoveredOption={hoveredOption}
                    onClick={onChange}
                    onMouseOver={setHoveredOption}
                  />
                )
              )(options)
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
): SelectOption<T> | undefined => {
  const flatOptions = flattenObj(options)
  const currentIndex = current
    ? flatOptions?.findIndex((o) => o.key === current.key)
    : -1
  const index = clamp(-1, flatOptions.length - 1)(currentIndex + steps)
  return index >= 0 ? flatOptions[index] : undefined
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
          className={cx(
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
