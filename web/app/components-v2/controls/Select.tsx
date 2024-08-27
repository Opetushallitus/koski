import * as A from 'fp-ts/Array'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as Ord from 'fp-ts/Ord'
import * as string from 'fp-ts/string'
import { pipe } from 'fp-ts/lib/function'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  KoodistokoodiviiteKoodistonNimellä,
  KoodistokoodiviiteKoodistonNimelläOrd
} from '../../appstate/koodisto'
import { Peruste } from '../../appstate/peruste'
import {
  TestIdLayer,
  useParentTestId,
  useTestId
} from '../../appstate/useTestId'
import { t } from '../../i18n/i18n'
import { Koodistokoodiviite } from '../../types/fi/oph/koski/schema/Koodistokoodiviite'
import { LocalizedString } from '../../types/fi/oph/koski/schema/LocalizedString'
import { nonNull } from '../../util/fp/arrays'
import { pluck } from '../../util/fp/objects'
import { koodistokoodiviiteId } from '../../util/koodisto'
import { clamp, sum } from '../../util/numbers'
import { textSearch } from '../../util/strings'
import { CommonProps, common, cx } from '../CommonProps'
import { Removable } from './Removable'

export type SelectProps<T> = CommonProps<{
  initialValue?: OptionKey
  value?: OptionKey
  options: OptionList<T>
  onChange: (option?: SelectOption<T>) => void
  onRemove?: (option: SelectOption<T>) => void
  onSearch?: (query: string) => void
  placeholder?: string | LocalizedString
  hideEmpty?: boolean
  disabled?: boolean
  autoselect?: boolean
  inlineOptions?: boolean
  testId: string | number
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

const optionExists = <T,>(options: OptionList<T>, key: string): boolean =>
  !!options.find(
    (o) =>
      o.key === key ||
      (o.children !== undefined && optionExists(o.children, key))
  )

export const Select = <T,>(props: SelectProps<T>) => {
  const inputTestId = useTestId(props.testId, 'input')
  const select = useSelectState(props)
  const input = useRef<HTMLInputElement>(null)

  const { options, onChange, value, initialValue, autoselect } = props
  useEffect(() => {
    if (autoselect) {
      if (optionsCount(options) < 2) {
        const first = firstOption(options)
        if (first?.key !== value) {
          onChange(first)
        }
      } else if (value === undefined && initialValue) {
        onChange(options.find((o) => o.key === initialValue))
      } else if (value !== undefined && !optionExists(options, value)) {
        onChange(undefined)
      }
    }
  }, [autoselect, initialValue, onChange, options, value])

  return (
    <TestIdLayer id={props.testId} wrap="div">
      <div
        // input-container -luokka on vanhan testiframeworkin kanssa yhteensopivuuden lisäämiseksi (kts. pageApi.js -> Input)
        {...common(props, ['Select', 'input-container'])}
        {...select.containerEventListeners}
      >
        <input
          className="Select__input"
          placeholder={t(props.placeholder || 'Valitse...')}
          value={select.filter === null ? select.displayValue : select.filter}
          type="search"
          autoComplete="off"
          disabled={
            props.disabled || (!props.onSearch && props.options.length === 0)
          }
          {...select.inputEventListeners}
          data-testid={inputTestId}
          ref={input}
        />
        {select.dropdownVisible && (
          <div className="Select__optionListContainer">
            <TestIdLayer wrap="div" id="options">
              <OptionList
                inputRef={input}
                options={select.options}
                hoveredOption={select.hoveredOption}
                onRemove={props.onRemove}
                inlineOptions={props.inlineOptions}
                {...select.dropdownEventListeners}
              />
            </TestIdLayer>
          </div>
        )}
      </div>
    </TestIdLayer>
  )
}

type OptionListProps<T> = CommonProps<{
  inputRef: React.RefObject<HTMLInputElement>
  options: OptionList<T>
  hoveredOption?: SelectOption<T>
  onClick: (o: SelectOption<T>, event: React.MouseEvent) => void
  onMouseOver: (o: SelectOption<T>, event: React.MouseEvent) => void
  onRemove?: (o: SelectOption<T>) => void
  inlineOptions?: boolean
}>

const OptionList = <T,>(props: OptionListProps<T>): React.ReactElement => {
  const parentTestId = useParentTestId()

  const onClick = (option: SelectOption<T>) => (event: React.MouseEvent) => {
    event.preventDefault()
    event.stopPropagation()
    props.onClick(option, event)
  }

  const { options, onRemove, ...rest } = props

  const [maxHeight, setMaxHeight] = useState(
    props.inlineOptions ? undefined : 300
  )
  useEffect(() => {
    const updateMaxHeight = () => {
      if (props.inputRef.current) {
        const h = window.innerHeight
        const rect = props.inputRef.current.getBoundingClientRect()
        setMaxHeight(clamp(50, 500)(h - rect.y - rect.height - 20))
      }
    }

    window.addEventListener('scroll', updateMaxHeight)
    window.addEventListener('resize', updateMaxHeight)
    updateMaxHeight()

    return () => {
      window.removeEventListener('scroll', updateMaxHeight)
      window.removeEventListener('resize', updateMaxHeight)
    }
  }, [props.inputRef])

  return (
    <ul
      {...common(props, [
        'Select__optionList',
        props.inlineOptions && 'Select__optionList--inline'
      ])}
      style={{ maxHeight }}
    >
      {options.map((opt) => (
        <TestIdLayer key={opt.key} id={opt.key}>
          <li
            className="Select__option"
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
                data-testid={`${parentTestId}.${opt.key}.item`}
              >
                {opt.display || opt.label}
              </div>
            </Removable>
            {opt.children && <OptionList options={opt.children} {...rest} />}
          </li>
        </TestIdLayer>
      ))}
    </ul>
  )
}

// State

const useSelectState = <T,>(props: SelectProps<T>) => {
  const [dropdownVisible, setDropdownVisible] = useState(false)
  const [displayValue, setDisplayValue] = useState<string>('')
  const [hoveredOption, onMouseOverOption] = useState<
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
      onMouseOverOption(flatOptions.arr.find((o) => !o.isGroup))
    }
  }, [flatOptions.arr, props.hideEmpty])

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

  const onChangeCb = props.onChange
  const onClickOption = useCallback(
    (option?: SelectOption<T>) => {
      setDropdownVisible(false)
      setFilter(null)
      onChangeCb(option)
    },
    [onChangeCb]
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
            onMouseOverOption(selectOption(flatOptions, hoveredOption, 1))
          }
          setDropdownVisible(true)
          event.preventDefault()
          event.stopPropagation()
          scrollHoveredIntoView(selectContainer)
          return
        case 'ArrowUp':
          if (dropdownVisible) {
            onMouseOverOption(selectOption(flatOptions, hoveredOption, -1))
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
            onClickOption(hoveredOption)
          }
          return
        default:
        // console.log(event.key)
      }
    },
    [dropdownVisible, flatOptions, hoveredOption, onClickOption]
  )

  const { hideEmpty, onSearch } = props
  const onUserType: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      setFilter(event.target.value)
      setDropdownVisible(true)
      const needle = event.target.value.toLowerCase()
      if (needle && !hideEmpty) {
        const firstMatch = flatOptions.arr.find((o) =>
          o.label.toLowerCase().includes(needle)
        )
        onMouseOverOption(firstMatch)
      } else {
        onMouseOverOption(undefined)
      }
      onSearch?.(event.target.value)
    },
    [flatOptions.arr, hideEmpty, onSearch]
  )

  return useMemo(
    () => ({
      displayValue,
      options,
      hoveredOption,
      filter,
      dropdownVisible,
      containerEventListeners: {
        ref: selectContainer,
        onFocus,
        onKeyDown,
        onBlur
      },
      inputEventListeners: {
        onChange: onUserType,
        onClick: onFocus
      },
      dropdownEventListeners: {
        onClick: onClickOption,
        onMouseOver: onMouseOverOption
      }
    }),
    [
      displayValue,
      dropdownVisible,
      filter,
      hoveredOption,
      onBlur,
      onClickOption,
      onFocus,
      onKeyDown,
      onUserType,
      options
    ]
  )
}

// Exported utils

export const groupKoodistoToOptions = <T extends string>(
  koodit: KoodistokoodiviiteKoodistonNimellä<T>[],
  ords?: Array<Ord.Ord<KoodistokoodiviiteKoodistonNimellä>>,
  format?: (koodi: KoodistokoodiviiteKoodistonNimellä) => string
): Array<SelectOption<Koodistokoodiviite<T>>> =>
  pipe(koodit, NEA.groupBy(pluck('koodistoNimi')), (grouped) =>
    Object.entries(grouped).map(([groupName, groupKoodit]) => ({
      key: groupName,
      label: groupName,
      isGroup: true,
      children: A.sortBy(ords || [KoodistokoodiviiteKoodistonNimelläOrd])(
        groupKoodit
      ).map((k) => ({
        key: k.id,
        label: format
          ? format(k)
          : t(k.koodiviite.nimi) || k.koodiviite.koodiarvo,
        value: k.koodiviite
      }))
    }))
  )

export const koodiviiteToOption = <T extends string>(
  koodiviite: Koodistokoodiviite<T>
): SelectOption<Koodistokoodiviite<T>> => ({
  key: koodistokoodiviiteId(koodiviite),
  value: koodiviite,
  label: t(koodiviite.nimi) || koodiviite.koodiarvo
})

export const perusteToOption = (peruste: Peruste): SelectOption<Peruste> => ({
  key: peruste.koodiarvo,
  value: peruste,
  label: [peruste.koodiarvo, t(peruste.nimi)].filter(nonNull).join(' ')
})

export const SelectOptionOrd = Ord.contramap((o: SelectOption<any>) => o.label)(
  string.Ord
)

export const sortOptions = <T,>(options: Array<SelectOption<T>>) =>
  A.sort(SelectOptionOrd)(options)

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
  const isMatch = textSearch(query)

  const matchesQuery = (option: SelectOption<T>): SelectOption<T> | null => {
    if (option.ignoreFilter) {
      return option
    }
    const children = option.children?.filter(matchesQuery)
    return (children && A.isNonEmpty(children)) || isMatch(option.label)
      ? option
      : null
  }

  return options.map(matchesQuery).filter(nonNull)
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

const optionCount = (option: SelectOption<any>): number => {
  const childCount = option.children ? optionsCount(option.children) : 0
  const selfCount = option.isGroup ? 0 : 1
  return selfCount + childCount
}

const optionsCount = (options: OptionList<any>): number =>
  sum(options.map(optionCount))

const firstOption = <T,>(
  options: OptionList<T>
): SelectOption<T> | undefined => {
  for (const option of options) {
    if (!option.isGroup) {
      return option
    } else if (option.children) {
      const child = firstOption(option.children)
      if (child) {
        return child
      }
    }
  }
}
