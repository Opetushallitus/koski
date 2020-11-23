import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import { AikajaksoValinta, LyhytKuvaus, RaportinLataus, Vinkit } from './raporttiComponents'

export const AikajaksoRaportti = ({organisaatioP, apiEndpoint, shortDescription, dateInputHelp, example}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const submitBus = Bacon.Bus()

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    organisaatioP, alkuAtom, loppuAtom, (o, a, l) => o && a && l && (l.valueOf() >= a.valueOf()) && {oppilaitosOid: o.oid, alku: formatISODate(a), loppu: formatISODate(l), password, baseUrl: `/koski/api/raportit${apiEndpoint}`})

  const downloadExcelE = submitBus.map(downloadExcelP).flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => showError(e))

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <AikajaksoValinta
        alkuAtom={alkuAtom}
        loppuAtom={loppuAtom}
        ohje={dateInputHelp}
      />

      <RaportinLataus
        password={password}
        inProgressP={inProgressP}
        submitEnabledP={submitEnabledP}
        submitBus={submitBus}
      />

      <Vinkit>{example}</Vinkit>
    </section>
  )
}
