import React, { useEffect, useRef, useState } from 'react'
import { scrollElementBottomVisible } from '../util/util'

type VuosiluokkaDropdownProps = {
  vuosiluokat: number[]
  valittu: number
  onValinta: (vuosiluokka: number) => void
}

export const VuosiluokkaDropdown: React.FC<VuosiluokkaDropdownProps> = ({
  vuosiluokat,
  valittu,
  onValinta
}) => {
  const [auki, setAuki] = useState(false)
  const [korostettu, setKorostettu] = useState(0)
  const lista = useRef<HTMLUListElement>(null)

  useEffect(() => {
    if (auki) {
      setTimeout(
        () => lista.current && scrollElementBottomVisible(lista.current)
      )
    }
  }, [auki])

  const valitse = (event: React.SyntheticEvent, vuosiluokka: number) => {
    event.preventDefault()
    event.stopPropagation()
    onValinta(vuosiluokka)
    setAuki(false)
    setKorostettu(0)
  }

  const käsitteleNäppäin = (event: React.KeyboardEvent) => {
    switch (event.key) {
      case 'ArrowUp':
        setKorostettu((i) => (i === 0 ? 0 : i - 1))
        break
      case 'ArrowDown':
        event.preventDefault()
        event.stopPropagation()
        if (auki) {
          setKorostettu((i) => (i === vuosiluokat.length - 1 ? i : i + 1))
        } else {
          setAuki(true)
        }
        break
      case 'Escape':
        setAuki(false)
        break
      case 'Enter':
        event.preventDefault()
        if (auki && korostettu < vuosiluokat.length) {
          valitse(event, vuosiluokat[korostettu])
        }
        break
    }
  }

  return (
    <span>
      <div
        className="dropdown"
        tabIndex={0}
        onBlur={() => setAuki(false)}
        onKeyDown={käsitteleNäppäin}
        role="combobox"
      >
        <div
          className="select"
          role="combobox"
          aria-label={String(valittu)}
          aria-owns="options-list"
          onClick={() => setAuki((wasAuki) => !wasAuki)}
        >
          {valittu}
        </div>
        <ul
          className={auki ? 'options open' : 'options'}
          aria-expanded={auki}
          ref={lista}
          id="options-list"
          data-testid="koodisto-dropdown-all-options"
          role="listbox"
        >
          {vuosiluokat.map((vuosiluokka, index) => (
            <li
              role="listitem"
              key={vuosiluokka}
              aria-label={String(vuosiluokka)}
              className={index === korostettu ? 'option selected' : 'option'}
              onMouseDown={(event) => valitse(event, vuosiluokka)}
              onClick={(event) => valitse(event, vuosiluokka)}
              onMouseOver={() => setKorostettu(index)}
            >
              {vuosiluokka}
            </li>
          ))}
        </ul>
      </div>
    </span>
  )
}
