import React from 'baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import Dropdown from '../components/Dropdown'
import { LyhytKuvaus, PaivaValinta, RaportinLataus, Vinkit } from './raporttiComponents'

export const VuosiluokkaRaporttiPaivalta = ({organisaatioP, apiEndpoint, description, example}) => {
  const paivaAtom = Atom()
  const vuosiluokkaAtom = Atom('1')
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    organisaatioP, paivaAtom, vuosiluokkaAtom,
    (o, p, v) => o && p && v && ({oppilaitosOid: o.oid, paiva: formatISODate(p), vuosiluokka:(v), password, baseUrl: `/koski/api/raportit${apiEndpoint}`})
  )
  const downloadExcelE = submitBus.map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => { showError(e) })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())
  const vuosiluokat = [1, 2, 3, 4, 5, 6, 7, 8, 9]

  return (
    <section>
      <LyhytKuvaus>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam porttitor libero dictum sem rhoncus, at euismod ex finibus. Morbi tortor purus, vehicula ut purus eget, blandit laoreet eros. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Proin tellus ipsum, mattis non purus sed, mattis rutrum arcu.
      </LyhytKuvaus>

      <PaivaValinta paivaAtom={paivaAtom} />

      <div className="dropdown-selection parametri vuosiluokka">
        <label><Text name="Valitse vuosiluokka"/></label>
        <VuosiluokkaDropdown
          value={vuosiluokkaAtom}
          vuosiluokat={vuosiluokat}
        />
      </div>

      <RaportinLataus
        password={password}
        inProgressP={inProgressP}
        submitEnabledP={submitEnabledP}
        submitBus={submitBus}
      />

      <Vinkit>
        <p>{description}</p>
        <p>{example}</p>
      </Vinkit>
    </section>
  )
}

const VuosiluokkaDropdown = ({value, vuosiluokat}) => (
  <div>
    {value.map(v => (
      <Dropdown
        options={vuosiluokat}
        keyValue={(key) => key}
        displayValue={(dVal) => dVal}
        selected={v}
        onSelectionChanged={(input) => value.set(input)}
      />))}
  </div>
)
