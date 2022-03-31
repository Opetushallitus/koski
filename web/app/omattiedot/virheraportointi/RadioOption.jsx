import React from 'baret'
import {t} from '../../i18n/i18n'

const OtherOppilaitosValue = 'other'

const RadioOption = ({selectedOppilaitosA, label, value, styleModifier, checked}) => (
  <li className={`oppilaitos-options__option${styleModifier ? `--${styleModifier}` : ''}`}>
    <input
      type='radio'
      name='oppilaitos'
      id={value}
      value={value}
      checked={checked || selectedOppilaitosA.map(oid => oid === value)}
      onChange={e => selectedOppilaitosA.set(e.target.value)}
    />

    <label htmlFor={value}>
      {label}
    </label>
  </li>
)

RadioOption.displayName = 'RadioOption'

const OppilaitosOption = ({oppilaitos, selectedOppilaitosA}) => (
  <RadioOption
    selectedOppilaitosA={selectedOppilaitosA}
    label={t(oppilaitos.nimi) + (oppilaitos.suoritus ? ` (${oppilaitos.suoritus.toLowerCase()})` : '')}
    value={oppilaitos.oid}
  />
)

OppilaitosOption.displayName = 'OppilaitosOption'

const MuuOppilaitosOptions = ({selectedOppilaitosA, isSelected}) => (
  <RadioOption
    selectedOppilaitosA={selectedOppilaitosA}
    label={t('Muu')}
    value={OtherOppilaitosValue}
    styleModifier='other'
    checked={isSelected}
  />
)

MuuOppilaitosOptions.displayName = 'MuuOppilaitosOptions'

export {
  OtherOppilaitosValue,
  OppilaitosOption,
  MuuOppilaitosOptions
}
