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

const reportTypes = {
  alkuvaihe: 'alkuvaihe',
  päättövaihe: 'päättövaihe',
  oppiaineenoppimäärä: 'oppiaineenoppimäärä'
}

export const AikuistenPerusopetuksenRaportit = ({organisaatioAtom, apiEndpoint, title, description}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const osasuoritustenAikarajausAtom = Atom(false)
  const raportinTyyppiAtom = Atom(reportTypes.alkuvaihe)
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    organisaatioAtom, alkuAtom, loppuAtom, osasuoritustenAikarajausAtom, raportinTyyppiAtom,
    (o, a, l, r, t) => o && a && l && (l.valueOf() >= a.valueOf()) && t && {
      oppilaitosOid: o.oid,
      alku: formatISODate(a),
      loppu: formatISODate(l),
      osasuoritustenAikarajaus: r,
      password,
      raportinTyyppi: t,
      baseUrl: `/koski/api/raportit${apiEndpoint}`
    })

  const downloadExcelE = submitBus.map(downloadExcelP).flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => showError(e))

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())

  return (
    <section>
      <h2>{title}</h2>
      <p>{description}</p>
      <div className='parametri'>
        <label><Text name='Raportin tyyppi'/></label>
      </div>
      {raportinTyyppiAtom.map(v => (
        <React.Fragment>
          <label className='radio-option-container'>
            <input className='radio-option' type='radio' checked={v === reportTypes.alkuvaihe} onChange={() => raportinTyyppiAtom.set(reportTypes.alkuvaihe)}/>
            <Text name='Alkuvaihe' />
          </label>
          <label className='radio-option-container'>
            <input className='radio-option' type='radio' checked={v === reportTypes.päättövaihe} onChange={() => raportinTyyppiAtom.set(reportTypes.päättövaihe)}/>
            <Text name='Päättövaihe' />
          </label>
          <label className='radio-option-container'>
            <input className='radio-option' type='radio' checked={v === reportTypes.oppiaineenoppimäärä} onChange={() => raportinTyyppiAtom.set(reportTypes.oppiaineenoppimäärä)}/>
            <Text name='Oppiaineen oppimäärä' />
          </label>
        </React.Fragment>
      ))}
      <div className='parametri'>
        <label><Text name='Aikajakso'/></label>
        <div className='date-range'>
          <DateInput value={alkuAtom.get()} valueCallback={(value) => alkuAtom.set(value)} validityCallback={(valid) => !valid && alkuAtom.set(undefined)}/>
          {' — '}
          <DateInput value={loppuAtom.get()} valueCallback={(value) => loppuAtom.set(value)} validityCallback={(valid) => !valid && loppuAtom.set(undefined)}/>
        </div>
      </div>
      {osasuoritustenAikarajausAtom.map(v => (
        <React.Fragment>
          <label className='radio-option-container'>
            <input className='radio-option' type='radio' checked={!v} onChange={() => osasuoritustenAikarajausAtom.set(false)}/>
            <Text name='Raportille valitaan kaikki tutkinnon osat riippumatta niiden suoritusajankohdasta' />
          </label>
          <label className='radio-option-container'>
            <input className='radio-option' type='radio' checked={v} onChange={() => osasuoritustenAikarajausAtom.set(true)}/>
            <Text name='Raportille valitaan vain sellaiset tutkinnon osat, joiden arviointipäivä osuu yllä määritellylle aikajaksolle' />
          </label>
        </React.Fragment>
      ))}
      <div className='password'><Text name='Excel-tiedosto on suojattu salasanalla'/> {password}</div>
      <RaporttiDownloadButton inProgressP={inProgressP} disabled={submitEnabledP.not()} onSubmit={e => { e.preventDefault(); submitBus.push(); return false }} />
    </section>
  )
}
