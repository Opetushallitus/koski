import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import { AikajaksoValinta, LyhytKuvaus, RaportinLataus, Vinkit } from './raporttiComponents'

export const AikajaksoRaportti = ({organisaatioP, apiEndpoint, description}) => {
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
      <LyhytKuvaus>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam porttitor libero dictum sem rhoncus, at euismod ex finibus. Morbi tortor purus, vehicula ut purus eget, blandit laoreet eros. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Proin tellus ipsum, mattis non purus sed, mattis rutrum arcu.
      </LyhytKuvaus>

      <AikajaksoValinta alkuAtom={alkuAtom} loppuAtom={loppuAtom} />

      <RaportinLataus
        password={password}
        inProgressP={inProgressP}
        submitEnabledP={submitEnabledP}
        submitBus={submitBus}
      />

      <Vinkit>
        <p>{description}</p>
      </Vinkit>
    </section>
  )
}
