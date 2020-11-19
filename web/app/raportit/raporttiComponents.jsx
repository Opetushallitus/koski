import React from 'baret'
import Text from '../i18n/Text'
import DateInput from '../date/DateInput'
import InfoIcon from '../icons/InfoIcon'
import RaporttiDownloadButton from './RaporttiDownloadButton'
import RadioButtons from '../components/RadioButtons'

export const LyhytKuvaus = ({ children }) => (
    <p className="lyhyt-kuvaus">
        {children}
    </p>
)

export const PaivaValinta = ({ paivaAtom, ohje }) => (
    <div className="parametri">
        <label><Text name="Valitse päivä"/></label>
        <DateInput
            value={paivaAtom.get()}
            valueCallback={(value) => paivaAtom.set(value)}
            validityCallback={(valid) => !valid && paivaAtom.set(undefined)}
        />
        {ohje && <div className="ohje">{ohje}</div>}
    </div>
)

export const AikajaksoValinta = ({ alkuAtom, loppuAtom, ohje }) => (
    <div className="parametri">
        <label><Text name="select-date-range"/></label>
        <div className="parametrit-kentat">
            <DateInput
                value={alkuAtom.get()}
                valueCallback={(value) => alkuAtom.set(value)}
                validityCallback={(valid) => !valid && alkuAtom.set(undefined)}
            />
            <span className="parametri-aikajakso-viiva">{' — '}</span>
            <DateInput
                value={loppuAtom.get()}
                valueCallback={(value) => loppuAtom.set(value)}
                validityCallback={(valid) => !valid && loppuAtom.set(undefined)}
            />
        </div>
        {ohje && <div className="ohje">{ohje}</div>}
    </div>
)

export const Listavalinta = ({ label, options, atom }) => (
    <div className="parametri">
        {label && <label><Text name={label} /></label>}
        <RadioButtons
            options={options}
            selected={atom}
            onSelectionChanged={selected => atom.set(selected.key)}
        />
    </div>
)

export const Vinkit = ({ children }) => (
    <div className="vinkit">
      <h3><InfoIcon /> <Text name="Vinkkejä ja ohjeita" /></h3>
      {children}
    </div>
)

export const RaportinLataus = ({
    password,
    inProgressP,
    submitEnabledP,
    submitBus
}) => (
    <div>
        <div className="password">
        <Text name='Excel-tiedosto on suojattu salasanalla'/> {password}<br />
        <Text name="Ota salasana itsellesi talteen" />
        </div>
        <RaporttiDownloadButton inProgressP={inProgressP} disabled={submitEnabledP.not()} onSubmit={e => { e.preventDefault(); submitBus.push(); return false }} />
    </div>
)