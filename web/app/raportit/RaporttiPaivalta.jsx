import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import { LyhytKuvaus, PaivaValinta, RaportinLataus, Vinkit } from './raporttiComponents'

export const RaporttiPaivalta = ({organisaatioP, apiEndpoint, description, example}) => {
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

  return (
    <section>
      <LyhytKuvaus>
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Nullam porttitor libero dictum sem rhoncus, at euismod ex finibus. Morbi tortor purus, vehicula ut purus eget, blandit laoreet eros. Vestibulum ante ipsum primis in faucibus orci luctus et ultrices posuere cubilia curae; Proin tellus ipsum, mattis non purus sed, mattis rutrum arcu.
      </LyhytKuvaus>

      <PaivaValinta paivaAtom={paivaAtom} />

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
