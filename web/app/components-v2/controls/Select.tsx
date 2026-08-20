import * as A from 'fp-ts/Array'
import * as NEA from 'fp-ts/NonEmptyArray'
import * as Ord from 'fp-ts/Ord'
import * as string from 'fp-ts/string'
import { pipe } from 'fp-ts/lib/function'
import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react'
import {
  KoodistokoodiviiteKoodistonNimellä,
  KoodistokoodiviiteKoodistonNimelläOrd,
  useKoodistot
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
import { koodistokoodiviiteId, koodiviiteId } from '../../util/koodisto'
import { clamp, sum } from '../../util/numbers'
import { coerceForSort, textSearch } from '../../util/strings'
import { common, CommonProps, cx } from '../CommonProps'
import { Removable } from './Removable'
import { Spinner } from '../texts/Spinner'
import { PaikallinenKoodi } from '../../types/fi/oph/koski/schema/PaikallinenKoodi'

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
  maxOptions?: number
  allowOpenUpwards?: boolean
  hasErrors?: boolean
  skipAutoFocus?: boolean
  /**
   * Kutsutaan kun käyttäjä tyhjentää syötekentän eikä valikossa ole
   * "Ei valintaa" -vaihtoehtoa. Select ei tiedä, miten kentän tyhjä tila
   * esitetään - esim. koodistokenttä palautuu koodiviitteeseen ilman
   * koodiarvoa, ei arvoon undefined - joten päätös jää kutsujalle.
   *
   * Ilman tätä tyhjennys ei muuta arvoa lainkaan: kenttä palautuu entiseen
   * arvoonsa kun valikko sulkeutuu.
   */
  onClear?: () => void
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
  // Jos tosi, vaihtoehto tyylitellään "lisää uusi" -toiminnoksi (sininen teksti, vrt. vanhan UI:n new-item)
  isAddNew?: boolean
}

export const LoadingOptions: OptionList<any> = []

export type SelectOption<T> = FlatOption<T> & {
  // Vaihtoehdolle/ryhmälle näytettävät alivaihtoehdot
  children?: OptionList<T>
}

export type OptionKey = string

// React 16:ssa ei ole useId:tä, joten aria-controlsin tarvitsema uniikki id
// juoksutetaan itse. Selainpuolen renderöinti vain, joten laskuri riittää.
let optionListIdCounter = 0

/**
 * "Ei valintaa" -vaihtoehto, jos kenttä sellaisen tarjoaa: ryhmittelemätön
 * vaihtoehto ilman arvoa (ks. KoodistoField/KoodistoSelect zeroValueOption).
 *
 * Sen olemassaolo on ainoa merkki siitä, että kentän arvon saa ylipäätään
 * poistaa. Ilman sitä tyhjentäminen ei saa nollata mallia: esimerkiksi
 * kieliaineen kieli on skeemassa pakollinen, ja undefined saisi koko
 * kielikentän katoamaan riviltä.
 */
const nollavalinta = <T,>(
  options: FlatOptionList<T>
): FlatOption<T> | undefined =>
  options.arr.find((o) => !o.isGroup && !o.isAddNew && o.value === undefined)

const optionExists = <T,>(options: OptionList<T>, key: string): boolean =>
  !!options.find(
    (o) =>
      o.key === key ||
      (o.children !== undefined && optionExists(o.children, key))
  )

export const Select = <T,>(props: SelectProps<T>) => {
  const inputTestId = useTestId(props.testId, 'input')
  const toggleTestId = useTestId(props.testId, 'toggle')
  const select = useSelectState(props)
  const input = useRef<HTMLInputElement>(null)
  const optionListId = useMemo(
    () => `Select__options-${(optionListIdCounter += 1)}`,
    []
  )

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

  const isLoading = props.options === LoadingOptions
  const disabled =
    isLoading ||
    props.disabled ||
    (!props.onSearch && props.options.length === 0)

  // Avauspainike on oma elementtinsä, jotta siitä voi sulkea auki olevan
  // valikon (ARIA APG:n combobox-kuvio: painike kertoo tilan aria-expandedilla
  // ja sen aktivointi sulkee avatun listan). Aiemmin kolmio oli pelkkä
  // ::after-pseudoelementti, eikä siihen voinut kohdistaa klikkausta.
  const onToggleClick = useCallback(() => {
    const avataan = !select.dropdownVisible
    select.toggleDropdown()
    // Näppäimistökäyttö jatkuu syötekentästä, mutta vain avattaessa: suljettaessa
    // fokusointi laukaisisi onFocusin, joka avaisi valikon heti uudelleen.
    if (avataan) {
      input.current?.focus()
    }
  }, [select])

  return (
    <TestIdLayer id={props.testId} wrap="div">
      <div
        // input-container -luokka on vanhan testiframeworkin kanssa yhteensopivuuden lisäämiseksi (kts. pageApi.js -> Input)
        {...common(props, [
          'Select',
          'input-container',
          props.onSearch && select.options.length === 0 && 'search',
          props.hasErrors && 'Select--error'
        ])}
        {...select.containerEventListeners}
      >
        <input
          className={`Select__input`}
          placeholder={t(props.placeholder || 'Valitse...')}
          value={select.filter === null ? select.displayValue : select.filter}
          type="search"
          autoComplete="off"
          disabled={disabled}
          data-skip-autofocus={props.skipAutoFocus ? '' : undefined}
          {...select.inputEventListeners}
          data-testid={inputTestId}
          ref={input}
        />
        <button
          type="button"
          className="Select__toggle"
          ref={select.toggleRef}
          // Painike ei ole oma tabulaattorikohteensa: syötekenttä on kontrolli,
          // jota näppäimistöllä käytetään.
          tabIndex={-1}
          aria-expanded={select.dropdownVisible}
          aria-controls={optionListId}
          aria-label={t('Näytä vaihtoehdot')}
          disabled={disabled}
          // Painike ei ole koskaan järkevä autofokuskohde: Modal fokusoi
          // ensimmäisen merkitsemättömän painikkeen, ja ilman tätä dialogi
          // avautuisi valikko auki (ks. containers/Modal.tsx).
          data-skip-autofocus=""
          // Fokus pysyy syötekentässä, jolloin klikkaus ei laukaise onFocusia
          // eikä avaa valikkoa ohi toggle-logiikan.
          onMouseDown={(event) => event.preventDefault()}
          // Nuolen fokusoiminen ei saa avata listaa, vaikka fokus tulisi
          // ohjelmallisesti: avaaminen kuuluu vain klikkaukselle.
          onFocus={(event) => event.stopPropagation()}
          onClick={onToggleClick}
          data-testid={toggleTestId}
        />
        {isLoading && <Spinner className="Select__spinner" />}
        {select.dropdownVisible && (
          <div className="Select__optionListContainer" id={optionListId}>
            <TestIdLayer wrap="div" id="options">
              <OptionList
                inputRef={input}
                options={select.options}
                hoveredOption={select.hoveredOption}
                onRemove={props.onRemove}
                inlineOptions={props.inlineOptions}
                maxOptions={props.maxOptions}
                allowOpenUpwards={props.allowOpenUpwards}
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
  maxOptions?: number
  allowOpenUpwards?: boolean
}>

const OptionList = <T,>(props: OptionListProps<T>): React.ReactElement => {
  const parentTestId = useParentTestId()

  const onClick = (option: SelectOption<T>) => (event: React.MouseEvent) => {
    event.preventDefault()
    event.stopPropagation()
    props.onClick(option, event)
  }

  const { options, onRemove, ...rest } = props

  const truncatedOptions = useMemo(
    () => (props.maxOptions ? A.takeLeft(props.maxOptions)(options) : options),
    [options, props.maxOptions]
  )

  const [maxHeight, setMaxHeight] = useState(
    props.inlineOptions ? undefined : 300
  )
  const [openUpwards, setOpenUpwards] = useState(false)

  useEffect(() => {
    const updateMaxHeight = () => {
      if (props.inputRef.current) {
        const h = window.innerHeight
        const rect = props.inputRef.current.getBoundingClientRect()
        const spaceBelow = h - rect.y - rect.height - 20
        const spaceAbove = rect.y - 20

        // Avaa ylöspäin jos alhaalla ei ole riittävästi tilaa mutta ylhäällä on enemmän
        // ja allowOpenUpwards on true
        const shouldOpenUpwards =
          props.allowOpenUpwards && spaceBelow < 150 && spaceAbove > spaceBelow
        setOpenUpwards(!!shouldOpenUpwards)

        const availableSpace = shouldOpenUpwards ? spaceAbove : spaceBelow
        setMaxHeight(clamp(50, 500)(availableSpace))
      }
    }

    window.addEventListener('scroll', updateMaxHeight)
    window.addEventListener('resize', updateMaxHeight)
    updateMaxHeight()

    return () => {
      window.removeEventListener('scroll', updateMaxHeight)
      window.removeEventListener('resize', updateMaxHeight)
    }
  }, [props.inputRef, props.allowOpenUpwards])

  return (
    <ul
      {...common(props, [
        'Select__optionList',
        props.inlineOptions && 'Select__optionList--inline',
        openUpwards && 'Select__optionList--upwards'
      ])}
      style={{ maxHeight }}
    >
      {truncatedOptions.map((opt) => (
        <TestIdLayer key={opt.key} id={opt.key}>
          <li
            className="Select__option"
            onClick={opt.isGroup ? undefined : onClick(opt)}
            onMouseDown={opt.isGroup ? undefined : (e) => e.preventDefault()}
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
                  opt.isGroup && 'Select__optionGroup',
                  opt.isAddNew && 'Select__optionLabel--addNew'
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
            {opt.children && (
              <OptionList
                options={opt.children}
                onRemove={onRemove}
                allowOpenUpwards={props.allowOpenUpwards}
                {...rest}
              />
            )}
          </li>
        </TestIdLayer>
      ))}
      {options.length !== truncatedOptions.length && (
        <li>{t('Osa tuloksista piilotettu. Rajaa tuloksia hakusanalla.')}</li>
      )}
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
  const toggleRef = useRef<HTMLButtonElement>(null)

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

  const onSearchProp = props.onSearch
  const onFocus = useCallback(() => {
    setDropdownVisible(true)
    if (onSearchProp) {
      setFilter('')
    }
  }, [onSearchProp])

  // Losing the focus

  const blurTimeoutRef = useRef<number | null>(null)

  /**
   * Sulje valikko ja unohda kirjoitettu hakusana.
   *
   * Syötekenttä näyttää hakusanan (filter) aina kun sellainen on, ja muuten
   * valitun arvon (displayValue). Jos hakusanaa ei nollata valikon sulkeutuessa,
   * kenttä jää näyttämään sitä vaikka valintaa ei tehty: esimerkiksi arvon
   * pyyhkiminen backspacella jättää kentän tyhjän näköiseksi, vaikka malliin jää
   * edellinen arvo. Tällöin lomake näyttää tyhjältä mutta on validi, eikä
   * käyttäjä saa virheilmoitusta puuttuvasta tiedosta.
   */
  const closeDropdown = useCallback(() => {
    setDropdownVisible(false)
    setFilter(null)
  }, [])

  const toggleDropdown = useCallback(() => {
    if (dropdownVisible) {
      closeDropdown()
    } else {
      setDropdownVisible(true)
      if (onSearchProp) {
        setFilter('')
      }
    }
  }, [closeDropdown, dropdownVisible, onSearchProp])

  const onBlur: React.FocusEventHandler = useCallback(
    (event) => {
      // Tarkistetaan, että fokus ei siirry komponentin sisälle
      if (
        !event.relatedTarget ||
        !selectContainer.current?.contains(event.relatedTarget as Node)
      ) {
        // Lyhennetty timeout: riittää että ehditään käsitellä klikki
        blurTimeoutRef.current = window.setTimeout(closeDropdown, 150)
      }
    },
    [closeDropdown]
  )

  const cancelBlur = useCallback(() => {
    if (blurTimeoutRef.current !== null) {
      clearTimeout(blurTimeoutRef.current)
      blurTimeoutRef.current = null
    }
  }, [])

  useEffect(() => {
    const mouseHandler = (event: MouseEvent) => {
      const isInside =
        event.target instanceof Element &&
        selectContainer.current?.contains(event.target)

      const isToggle =
        event.target instanceof Element &&
        toggleRef.current?.contains(event.target)

      if (isToggle) {
        // Avauspainike vaihtaa tilan itse, joten täällä ei saa pakottaa auki.
        cancelBlur()
      } else if (isInside) {
        // Jos klikataan sisällä, peruuta blur ja pidä auki
        cancelBlur()
        setDropdownVisible(true)
      } else {
        // Jos klikataan ulkopuolella, sulje
        closeDropdown()
      }
    }
    document.body.addEventListener('click', mouseHandler)
    return () => {
      document.body.removeEventListener('click', mouseHandler)
      if (blurTimeoutRef.current !== null) {
        clearTimeout(blurTimeoutRef.current)
      }
    }
  }, [cancelBlur, closeDropdown])

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
        : queryOptions(props.options, filter)
    // Remove one level of grouping if only one group is present
    return opts.length === 1 && opts[0].isGroup ? opts[0].children || [] : opts
  }, [filter, props.options])

  // Interaction

  const onKeyDown: React.KeyboardEventHandler = useCallback(
    (event) => {
      switch (event.key) {
        case 'Tab':
          closeDropdown()
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
          closeDropdown()
          event.preventDefault()
          event.stopPropagation()
          return
        case 'Enter':
          event.preventDefault()
          event.stopPropagation()
          // Enter vahvistaa korostetun vaihtoehdon. Ilman korostusta valikko
          // vain suljetaan: aiemmin kutsuttiin onClickOption(undefined), joka
          // nollasi arvon myös kentissä joissa tyhjä ei ole sallittu - esim.
          // kieliaineen kieli katosi riviltä kokonaan.
          if (dropdownVisible && hoveredOption) {
            onClickOption(hoveredOption)
          } else {
            closeDropdown()
          }
          return
        default:
        // console.log(event.key)
      }
    },
    [closeDropdown, dropdownVisible, flatOptions, hoveredOption, onClickOption]
  )

  const { hideEmpty, onSearch, onClear: onClearProp } = props
  const onUserType: React.ChangeEventHandler<HTMLInputElement> = useCallback(
    (event) => {
      setFilter(event.target.value)
      setDropdownVisible(true)

      // Kentän tyhjentäminen palauttaa sen valitsemattomaan tilaan: joko
      // "Ei valintaa" -vaihtoehtoon tai kutsujan määrittelemään tyhjään
      // arvoon (onClear). Jos kumpaakaan ei ole, arvo jää ennalleen ja kenttä
      // palautuu siihen valikon sulkeutuessa.
      if (event.target.value === '') {
        const nolla = nollavalinta(flatOptions)
        if (nolla) {
          onChangeCb(nolla)
        } else {
          onClearProp?.()
        }
      }

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
    [flatOptions, hideEmpty, onChangeCb, onClearProp, onSearch]
  )

  return useMemo(
    () => ({
      displayValue,
      options,
      hoveredOption,
      filter,
      dropdownVisible,
      toggleDropdown,
      toggleRef,
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
      options,
      toggleDropdown
    ]
  )
}

// Exported utils

export const useKoodistoOptions = <T extends string>(
  ...koodistoUris: T[]
): SelectOption<Koodistokoodiviite<T>>[] => {
  const koodisto = useKoodistot(...koodistoUris)
  return useMemo(
    () => (koodisto ? groupKoodistoToOptions(koodisto) : LoadingOptions),
    [koodisto]
  ) as SelectOption<Koodistokoodiviite<T>>[]
}

export const useKoodistoOptionsWithFormat = <T extends string>(
  koodistoUris: T[],
  format: (koodi: KoodistokoodiviiteKoodistonNimellä) => string
): SelectOption<Koodistokoodiviite<T>>[] => {
  const koodisto = useKoodistot(...koodistoUris)
  return useMemo(
    () =>
      koodisto
        ? groupKoodistoToOptions(koodisto, undefined, format)
        : LoadingOptions,
    [koodisto, format]
  ) as SelectOption<Koodistokoodiviite<T>>[]
}

export const optionGroup = <T,>(
  label: string,
  children: SelectOption<T>[]
): SelectOption<T> => ({
  key: label,
  label,
  isGroup: true,
  children
})

export const regroupKoodisto = <T extends string>(
  koodit: KoodistokoodiviiteKoodistonNimellä<T>[],
  getGroup: (k: KoodistokoodiviiteKoodistonNimellä<T>) => string | null
) =>
  koodit.flatMap((k) => {
    const group = getGroup(k)
    return group === null ? [] : [{ ...k, koodistoNimi: group }]
  })

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

export const paikallinenKoodiToOption = (
  koodi: PaikallinenKoodi,
  options?: Partial<SelectOption<PaikallinenKoodi>>
): SelectOption<PaikallinenKoodi> => ({
  key: koodiviiteId(koodi),
  value: koodi,
  label: t(koodi.nimi) || koodi.koodiarvo,
  ...options
})

export const perusteToOption = (peruste: Peruste): SelectOption<Peruste> => ({
  key: peruste.koodiarvo,
  value: peruste,
  label: [peruste.koodiarvo, t(peruste.nimi)].filter(nonNull).join(' ')
})

export const SelectOptionOrd = Ord.contramap((o: SelectOption<any>) =>
  coerceForSort(o.label)
)(string.Ord)

export const sortOptions = <T,>(
  options: Array<SelectOption<T>>
): Array<SelectOption<T>> =>
  pipe(
    options,
    A.sort(SelectOptionOrd),
    A.map((o) => ({
      ...o,
      children: o.children && sortOptions(o.children)
    }))
  )

export const mapOptions =
  <T, S>(f: (o: SelectOption<T>) => SelectOption<S>) =>
  (options: Array<SelectOption<T>>): Array<SelectOption<S>> =>
    options.map((o) => ({
      ...f(o),
      children: o.children && mapOptions(f)(o.children)
    }))

export const mapOptionLabels = <T,>(f: (o: SelectOption<T>) => string) =>
  mapOptions((o: SelectOption<T>) => ({ ...o, label: f(o) }))

export const filterOptions =
  <T,>(f: (o: SelectOption<T>) => boolean) =>
  (options: Array<SelectOption<T>>): Array<SelectOption<T>> =>
    options.flatMap((o) => {
      const children = o.children && filterOptions(f)(o.children)
      return (children?.length || 0) > 0 || f(o) ? [{ ...o, children }] : []
    })

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

const queryOptions = <T,>(
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
      ? { ...option, children }
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
