import React, { useMemo, useState } from 'react'
import { buildClassNames } from '../components/classnames'
import { t } from '../i18n/i18n'
import {
  litistäOrganisaatiot,
  NäkyväOrganisaatio,
  rajaaHakusanalla
} from './raporttivalinta'

type OrganisaatioDropdownProps = {
  organisaatiot: NäkyväOrganisaatio[]
  valittu: NäkyväOrganisaatio | undefined
  onValinta: (organisaatio: NäkyväOrganisaatio) => void
}

export const OrganisaatioDropdown: React.FC<OrganisaatioDropdownProps> = ({
  organisaatiot,
  valittu,
  onValinta
}) => {
  const [auki, setAuki] = useState(false)
  const [hakusana, setHakusana] = useState<string | null>(null)

  const rajatutOrganisaatiot = useMemo(
    () =>
      hakusana ? rajaaHakusanalla(hakusana, organisaatiot) : organisaatiot,
    [hakusana, organisaatiot]
  )

  const avaa = () => {
    setAuki(true)
    setHakusana(null)
  }

  const valitse = (organisaatio: NäkyväOrganisaatio) => {
    setAuki(false)
    onValinta(organisaatio)
  }

  const siirräValintaa = (siirtymä: number) => {
    if (valittu) {
      const lista = litistäOrganisaatiot(rajatutOrganisaatiot)
      const uusiIndeksi =
        lista.findIndex((organisaatio) => organisaatio.oid === valittu.oid) +
        siirtymä
      if (uusiIndeksi >= 0 && uusiIndeksi < lista.length) {
        onValinta(lista[uusiIndeksi])
      }
    }
  }

  const käsitteleNäppäin = (event: React.KeyboardEvent) => {
    switch (event.key) {
      case 'ArrowUp':
        siirräValintaa(-1)
        break
      case 'ArrowDown':
        siirräValintaa(1)
        break
      case 'Enter':
        setAuki(false)
        break
    }
  }

  return (
    <div
      className="organisaatio-dropdown"
      onFocus={avaa}
      onBlur={() => setAuki(false)}
    >
      <div className="input-container">
        <input
          type="text"
          placeholder="Valitse..."
          className="select"
          value={(auki ? hakusana : valittu && t(valittu.nimi)) || ''}
          onKeyDown={käsitteleNäppäin}
          onChange={(event) =>
            setHakusana(
              event.target.value.length > 0 ? event.target.value : null
            )
          }
          onClick={avaa}
        />
      </div>
      <Vaihtoehdot
        organisaatiot={rajatutOrganisaatiot}
        valittu={valittu}
        onValinta={valitse}
        auki={auki}
      />
    </div>
  )
}

type VaihtoehdotProps = {
  organisaatiot: NäkyväOrganisaatio[]
  valittu: NäkyväOrganisaatio | undefined
  onValinta: (organisaatio: NäkyväOrganisaatio) => void
  auki?: boolean
  alataso?: boolean
}

const Vaihtoehdot: React.FC<VaihtoehdotProps> = ({
  organisaatiot,
  valittu,
  onValinta,
  auki,
  alataso
}) => (
  <ul className={alataso ? 'child-options' : auki ? 'options open' : 'options'}>
    {organisaatiot.map((organisaatio) => {
      const valitse = organisaatio.selectable
        ? () => onValinta(organisaatio)
        : undefined

      return (
        <li key={organisaatio.oid} className="option">
          <span
            className={buildClassNames([
              'value',
              valittu && organisaatio.oid === valittu.oid ? 'selected' : null,
              organisaatio.selectable ? null : 'disabled'
            ])}
            onMouseDown={valitse}
            onTouchStart={valitse}
            onClick={valitse}
          >
            {t(organisaatio.nimi)}
          </span>
          {organisaatio.children.length === 0 ? null : (
            <Vaihtoehdot
              organisaatiot={organisaatio.children}
              valittu={valittu}
              onValinta={onValinta}
              alataso
            />
          )}
        </li>
      )
    })}
  </ul>
)
