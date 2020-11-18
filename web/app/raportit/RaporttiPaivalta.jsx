import React from 'baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import DateInput from '../date/DateInput'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import RaporttiDownloadButton from './RaporttiDownloadButton'

export const RaporttiPaivalta = ({organisaatioP, apiEndpoint, title, description, example}) => {
  const paivaAtom = Atom()
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    organisaatioP, paivaAtom,
    (o, p) => o && p && ({oppilaitosOid: o.oid, paiva: formatISODate(p), password, baseUrl: `/koski/api/raportit${apiEndpoint}`})
  )
  const downloadExcelE = submitBus.map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => { showError(e) })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())

  return (<section>
    <h2>{title}</h2>
    <p>{description}</p>
    <p>{example}</p>
    <div className='parametri'>
      <label><Text name='Päivä'/></label>
      <DateInput value={paivaAtom.get()} valueCallback={(value) => paivaAtom.set(value)} validityCallback={(valid) => !valid && paivaAtom.set(undefined)} />
    </div>
    <div className='password'><Text name='Excel-tiedosto on suojattu salasanalla'/> {password}</div>
    <RaporttiDownloadButton inProgressP={inProgressP} disabled={submitEnabledP.not()} onSubmit={e => { e.preventDefault(); submitBus.push(); return false }} />
  </section>)
}
