import React from 'baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import DateInput from '../date/DateInput'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import Dropdown from '../components/Dropdown'
import RaporttiDownloadButton from './RaporttiDownloadButton'

export const VuosiluokkaRaporttiPaivalta = ({organisaatioAtom, apiEndpoint, title, description, example}) => {
  const paivaAtom = Atom()
  const vuosiluokkaAtom = Atom('')
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    organisaatioAtom, paivaAtom, vuosiluokkaAtom,
    (o, p, v) => o && p && v && ({oppilaitosOid: o.oid, paiva: formatISODate(p), vuosiluokka:(v), password, baseUrl: `/koski/api/raportit${apiEndpoint}`})
  )
  const downloadExcelE = submitBus.map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => { showError(e) })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())
  const vuosiluokat = [1, 2, 3, 4, 5, 6, 7, 8, 10]

  return (<section>
    <h2>{title}</h2>
    <p>{description}</p>
    <p>{example}</p>
      <div className='parametri'>
        <label><Text name='Päivä'/></label>
        <DateInput value={paivaAtom.get()} valueCallback={(value) => paivaAtom.set(value)} validityCallback={(valid) => !valid && paivaAtom.set(undefined)} />
      </div>
    <div className='dropdown-selection parametri'>
      <label><Text name='Vuosiluokka'/></label>
      <VuosiluokkaDropdown value={vuosiluokkaAtom} vuosiluokat={vuosiluokat}/>
    </div>
    <div className='password'><Text name='Excel-tiedosto on suojattu salasanalla'/> {password}</div>
    <RaporttiDownloadButton inProgressP={inProgressP} disabled={submitEnabledP.not()} onSubmit={e => { e.preventDefault(); submitBus.push(); return false }} />
  </section>)
}

const VuosiluokkaDropdown = ({value, vuosiluokat}) => (
  <div>
    {value.map(v => (
      <Dropdown
        options={vuosiluokat}
        keyValue={(key) => key}
        displayValue={(dVal) => dVal === 10 ? 'Peruskoulun päättävät' : dVal}
        selected={v}
        onSelectionChanged={(input) => value.set(input)}
      />))}
  </div>
)
