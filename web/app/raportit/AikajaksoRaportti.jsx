import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { showError } from '../util/location'
import { formatISODate } from '../date/date'
import { generateRandomPassword } from '../util/password'
import { downloadExcel } from './downloadExcel'
import {
  AikajaksoValinta,
  LyhytKuvaus,
  RaportinLataus,
  Vinkit
} from './raporttiComponents'
import { selectFromState } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'

export const AikajaksoRaportti = ({
  stateP,
  apiEndpoint,
  shortDescription,
  dateInputHelp,
  example,
  massaluovutusKyselymalli,
  lang
}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const submitBus = Bacon.Bus()

  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    selectedOrganisaatioP,
    alkuAtom,
    loppuAtom,
    (o, a, l) =>
      o &&
      a &&
      l &&
      l.valueOf() >= a.valueOf() && {
        oppilaitosOid: o.oid,
        alku: formatISODate(a),
        loppu: formatISODate(l),
        lang,
        password,
        baseUrl: `/koski/api/raportit${apiEndpoint}`
      }
  )

  const massaluovutusParametritP = Bacon.combineWith(
    selectedOrganisaatioP,
    alkuAtom,
    loppuAtom,
    (o, a, l) =>
      o && a && l && l.valueOf() >= a.valueOf()
        ? {
            organisaatioOid: o.oid,
            alku: formatISODate(a),
            loppu: formatISODate(l),
            language: lang
          }
        : null
  )

  const downloadExcelE = submitBus
    .map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError((e) => showError(e))

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map((x) => !!x).and(inProgressP.not())

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <AikajaksoValinta
        alkuAtom={alkuAtom}
        loppuAtom={loppuAtom}
        ohje={dateInputHelp}
      />

      {massaluovutusKyselymalli ? (
        Bacon.combineWith(
          massaluovutusParametritP,
          dbUpdatedP,
          organisaatioNimetP,
          (parametrit, dbUpdated, oppilaitosNimet) => (
            <MassaluovutusRaportinLataus
              kyselymalli={massaluovutusKyselymalli}
              parametrit={parametrit}
              dbUpdated={dbUpdated}
              oppilaitosNimet={oppilaitosNimet}
            />
          )
        )
      ) : (
        <RaportinLataus
          password={password}
          inProgressP={inProgressP}
          submitEnabledP={submitEnabledP}
          submitBus={submitBus}
          dbUpdatedP={dbUpdatedP}
        />
      )}

      <Vinkit>{example}</Vinkit>
    </section>
  )
}
