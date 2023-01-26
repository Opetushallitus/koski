import * as A from 'fp-ts/Array'
import { flow, pipe } from 'fp-ts/lib/function'
import * as NEA from 'fp-ts/NonEmptyArray'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import { KoodistokoodiviiteKoodistonNimellä } from '../../appstate/koodisto'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { KoodiViite } from '../../types/fi/oph/koski/schema/KoodiViite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { nonNull } from '../../util/fp/arrays'
import { pluck } from '../../util/fp/objects'
import { KoodiviiteWithOptionalUri } from '../../util/koodisto'
import { clamp } from '../../util/numbers'
import { common, CommonProps, cx } from '../CommonProps'
import { Removable } from './Removable'

export type SelectProps<T> = CommonProps<{
  initialValue?: OptionKey
  value?: OptionKey
  options: OptionList<T>
  onChange: (option?: SelectOption<T>) => void
  onRemove?: (option: SelectOption<T>) => void
  placeholder?: string | LocalizedString
  hideEmpty?: boolean
}>

export type OptionList<T> = Array<SelectOption<T>>

export type FlatOptionList<T> = { arr: Array<FlatOption<T>> }

export type FlatOption<T> = {
  // Uniikki tunnisteavain, joka erottaa eri vaihtoehdot toisistaan
  key: OptionKey
  // Puhtaasti tekstimuotoinen näytettävä arvo
  label: string
  // Muotoiltu näytettävä arvo
  display?: React.ReactNode
  // Vaihtoehtoon sidottu vapaamuotoinen data
  value?: T
  // Jos tosi, filtteri ei vaikuta tähän vaihtoehtoon (näkyy aina)
  ignoreFilter?: boolean
  // Jos tosi, tämä vaihtoehto ei ole valittavissa, vaan toimii ainoastaan ryhmän otsikkona
  isGroup?: boolean
  // Jos tosi, näytetään poistosymboli nimen vieressä, jonka klikkaaminen kutsuu Selectin callbackia onRemove
  removable?: boolean
}

export type SelectOption<T> = FlatOption<T> & {
  // Vaihtoehdolle/ryhmälle näytettävät alivaihtoehdot
  children?: OptionList<T>
}

export type OptionKey = string

export const Select = <T,>(props: SelectProps<T>) => {
  const [dropdownVisible, setDropdownVisible] = useState(false)
  const [displayValue, setDisplayValue] = useState<string>('')
  const [hoveredOption, setHoveredOption] = useState<
    SelectOption<T> | undefined
  >()

  const [filter, setFilter] = useState<string | null>(null)
  const selectContainer = useRef<HTMLDivElement>(null)

  const flatOptions = useMemo(
    () => flattenOptions(props.options),
    [props.options]
  )

  useEffect(() => {
    const option =
      props.value && flatOptions.arr.find((o) => o.key === props.value)
    setDisplayValue(option ? option.label : '')
  }, [props.value, flatOptions])

  useEffect(() => {
    if (props.hideEmpty) {
      setHoveredOption(flatOptions.arr.find((o) => !o.isGroup))
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

  const options: OptionList<T> = useMemo(() => {
    const opts =
      filter === '' || filter === null
        ? props.options
        : filterOptions(props.options, filter)
    // Remove one level of grouping if only one group is present
    return opts.length === 1 && opts[0].isGroup ? opts[0].children || [] : opts
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
            setHoveredOption(selectOption(flatOptions, hoveredOption, 1))
          }
          setDropdownVisible(true)
          event.preventDefault()
          event.stopPropagation()
          scrollHoveredIntoView(selectContainer)
          return
        case 'ArrowUp':
          if (dropdownVisible) {
            setHoveredOption(selectOption(flatOptions, hoveredOption, -1))
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
    [flatOptions, hoveredOption, dropdownVisible]
  )

  const onUserType: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      setFilter(event.target.value)
      setDropdownVisible(true)
      const needle = event.target.value.toLowerCase()
      if (needle && !props.hideEmpty) {
        const firstMatch = flatOptions.arr.find((o) =>
          o.label.toLowerCase().includes(needle)
        )
        setHoveredOption(firstMatch)
      } else {
        setHoveredOption(undefined)
      }
    },
    [flatOptions, props.hideEmpty]
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
        placeholder={t(props.placeholder || 'Valitse...')}
        value={filter === null ? displayValue : filter}
        onChange={onUserType}
        type="search"
        autoComplete="off"
      />
      {dropdownVisible && (
        <div className="Select__optionListContainer">
          <OptionList
            options={options}
            hoveredOption={hoveredOption}
            onClick={onChange}
            onMouseOver={setHoveredOption}
            onRemove={props.onRemove}
          />
        </div>
      )}
    </div>
  )
}

type OptionListProps<T> = {
  options: OptionList<T>
  hoveredOption?: SelectOption<T>
  onClick: (o: SelectOption<T>, event: React.MouseEvent) => void
  onMouseOver: (o: SelectOption<T>, event: React.MouseEvent) => void
  onRemove?: (o: SelectOption<T>) => void
}

const OptionList = <T,>(props: OptionListProps<T>): React.ReactElement => {
  const onClick = (option: SelectOption<T>) => (event: React.MouseEvent) => {
    event.preventDefault()
    event.stopPropagation()
    props.onClick(option, event)
  }

  const { options, onRemove, ...rest } = props

  return (
    <ul className="Select__optionList">
      {options.map((opt) => (
        <li
          className="Select__option"
          key={opt.key}
          onClick={opt.isGroup ? undefined : onClick(opt)}
        >
          <Removable
            isRemovable={Boolean(opt.removable && props.onRemove)}
            onClick={() => onRemove?.(opt)}
          >
            <div
              className={cx(
                'Select__optionLabel',
                props.hoveredOption?.key === opt.key &&
                  'Select__optionLabel--hover',
                opt.isGroup && 'Select__optionGroup'
              )}
              onMouseOver={
                opt.isGroup
                  ? undefined
                  : (event) => props.onMouseOver(opt, event)
              }
            >
              {opt.display || opt.label}
            </div>
          </Removable>
          {opt.children && <OptionList options={opt.children} {...rest} />}
        </li>
      ))}
    </ul>
  )
}

// Exported utils

export const groupKoodistoToOptions: <T extends string>(
  koodit: KoodistokoodiviiteKoodistonNimellä<T>[]
) => Array<SelectOption<Koodistokoodiviite<T>>> = flow(
  NEA.groupBy(pluck('koodistoNimi')),
  (grouped) =>
    Object.entries(grouped).map(([groupName, koodit]) => ({
      key: groupName,
      label: groupName,
      isGroup: true,
      children: koodit.map((k) => ({
        key: k.id,
        label: t(k.koodiviite.nimi) || k.koodiviite.koodiarvo,
        value: k.koodiviite
      }))
    }))
)

// Internal utils

const selectOption = <T,>(
  flatOptions: FlatOptionList<T>,
  current: SelectOption<T> | undefined,
  steps: number
): SelectOption<T> | undefined => {
  const currentIndex = current
    ? flatOptions.arr.findIndex((o) => o.key === current.key)
    : -1
  const index = clamp(-1, flatOptions.arr.length - 1)(currentIndex + steps)
  const option = index >= 0 ? flatOptions.arr[index] : undefined
  return option?.isGroup ? selectOption(flatOptions, option, steps) : option
}

const flattenOptions = <T,>(options: OptionList<T>): FlatOptionList<T> => {
  const flatten = (option: SelectOption<T>): FlatOption<T>[] => {
    const { children, ...flatOption } = option
    const x: FlatOption<T> = flatOption
    return [x, ...(children?.flatMap(flatten) || [])]
  }
  return { arr: options.flatMap(flatten) }
}

const filterOptions = <T,>(
  options: OptionList<T>,
  query: string
): OptionList<T> => {
  const needle = query.toLowerCase().trim()

  const matchesNeedle = (option: SelectOption<T>): SelectOption<T> | null => {
    const children = option.children?.filter(matchesNeedle)
    return (children && A.isNonEmpty(children)) ||
      option.label.toLowerCase().includes(needle)
      ? {
          ...option,
          children
        }
      : null
  }

  return options.map(matchesNeedle).filter(nonNull)
}

const scrollHoveredIntoView = (
  selectContainer: React.RefObject<HTMLDivElement>
) => {
  setTimeout(() => {
    selectContainer.current
      ?.querySelector('.Select__optionLabel--hover')
      ?.scrollIntoView({ block: 'nearest' })
  }, 0)
}
