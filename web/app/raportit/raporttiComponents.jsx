import React from '../util/baret'
import Text from '../i18n/Text'
import DateInput from '../date/DateInput'
import { InfoIcon } from '../icons/InfoIcon'
import RadioButtons from '../components/RadioButtons'

export const LyhytKuvaus = ({ children }) =>
  children ? <p className="lyhyt-kuvaus">{children}</p> : null

export const PaivaValinta = ({ paivaAtom, ohje, label, inputId }) => (
  <div className="parametri">
    <label>{label || <Text name="select-date" />}</label>
    <DateInput
      inputId={inputId}
      value={paivaAtom.get()}
      valueCallback={(value) => paivaAtom.set(value)}
      validityCallback={(valid) => !valid && paivaAtom.set(undefined)}
    />
    {ohje && <div className="ohje">{ohje}</div>}
  </div>
)

export const AikajaksoValinta = ({ alkuAtom, loppuAtom, ohje }) => (
  <div className="parametri">
    <label>
      <Text name="select-date-range" />
    </label>
    <div className="parametrit-kentat">
      <DateInput
        inputId="dateinput-alku"
        value={alkuAtom.get()}
        valueCallback={(value) => alkuAtom.set(value)}
        validityCallback={(valid) => !valid && alkuAtom.set(undefined)}
      />
      <span className="parametri-aikajakso-viiva">{' — '}</span>
      <DateInput
        inputId="dateinput-loppu"
        value={loppuAtom.get()}
        valueCallback={(value) => loppuAtom.set(value)}
        validityCallback={(valid) => !valid && loppuAtom.set(undefined)}
      />
    </div>
    {ohje && <div className="ohje">{ohje}</div>}
  </div>
)

export const Listavalinta = ({ label, help, options, atom, seamless }) => (
  <div className={`parametri ${seamless ? 'parametri-seamless' : ''}`}>
    {label && (
      <label>
        <Text name={label} />
      </label>
    )}
    {help && <div className="etuohje">{<Text name={help} />}</div>}
    <RadioButtons
      options={options}
      selected={atom}
      onSelectionChanged={(selected) => atom.set(selected.key)}
    />
  </div>
)

export const Vinkit = ({ children }) =>
  children ? (
    <div className="vinkit">
      <h3>
        <InfoIcon /> <Text name="tips-and-help" />
      </h3>
      {children}
    </div>
  ) : null
