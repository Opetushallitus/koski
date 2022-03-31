import React from 'baret'
import Text from '../i18n/Text'
import DateInput from '../date/DateInput'
import InfoIcon from '../icons/InfoIcon'
import RaporttiDownloadButton from './RaporttiDownloadButton'
import RadioButtons from '../components/RadioButtons'
import { formatFinnishDateTime } from '../../app/date/date'
import { t } from '../i18n/i18n'

export const LyhytKuvaus = ({ children }) => children
    ? (
        <p className="lyhyt-kuvaus">
            {children}
        </p>
    )
    : null

LyhytKuvaus.displayName = 'LyhytKuvaus'

export const PaivaValinta = ({ paivaAtom, ohje }) => (
    <div className="parametri">
        <label><Text name="select-date"/></label>
        <DateInput
            value={paivaAtom.get()}
            valueCallback={(value) => paivaAtom.set(value)}
            validityCallback={(valid) => !valid && paivaAtom.set(undefined)}
        />
        {ohje && <div className="ohje">{ohje}</div>}
    </div>
)

PaivaValinta.displayName = 'PaivaValinta'

export const AikajaksoValinta = ({ alkuAtom, loppuAtom, ohje }) => (
    <div className="parametri">
        <label><Text name="select-date-range"/></label>
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

AikajaksoValinta.displayName = 'AikajaksoValinta'

export const Listavalinta = ({ label, help, options, atom, seamless }) => (
    <div className={`parametri ${seamless ? 'parametri-seamless' : ''}`}>
        {label && <label><Text name={label} /></label>}
        {help && (<div className="etuohje">{<Text name={help} />}</div>)}
        <RadioButtons
            options={options}
            selected={atom}
            onSelectionChanged={selected => atom.set(selected.key)}
        />
    </div>
)

Listavalinta.displayName = 'Listavalinta'

export const Vinkit = ({ children }) => children
    ? (
        <div className="vinkit">
        <h3><InfoIcon /> <Text name="tips-and-help" /></h3>
        {children}
        </div>
    )
    : null

Vinkit.displayName = 'Vinkit'

export const RaportinLataus = ({
    password,
    dbUpdatedP,
    inProgressP,
    submitEnabledP,
    submitBus
}) => (
    <div className="raportin-lataus">
        <div className="password">
            <Text name='Excel-tiedosto on suojattu salasanalla'/> {password}<br />
            <Text name="Ota salasana itsellesi talteen" />
        </div>
        {dbUpdatedP.map(dbUpdated => {
            const text = t('Raportti pohjautuu KOSKI-tietovarannossa hetkellä $DATETIME olleille tiedoille.')
            const [head, foot] = text.split('$DATETIME')
            return (
                <div className="update-time">
                    {head}
                    <span className="datetime">{formatFinnishDateTime(new Date(dbUpdated))}</span>
                    {foot}
                </div>
            )
        })}
        <RaporttiDownloadButton inProgressP={inProgressP} disabled={submitEnabledP.not()} onSubmit={e => { e.preventDefault(); submitBus.push(); return false }} />
    </div>
)

RaportinLataus.displayName = 'RaportinLataus'
