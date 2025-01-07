import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { showError } from '../util/location'
import { formatISODate } from '../date/date'
import { generateRandomPassword } from '../util/password'
import { downloadExcel } from './downloadExcel'
import {
  LyhytKuvaus,
  PaivaValinta,
  RaportinLataus,
  Vinkit
} from './raporttiComponents'
import { isEqualDate, selectFromState, today } from './raporttiUtils'
import { t } from '../i18n/i18n'

export const RaporttiPaivalta = ({
  stateP,
  apiEndpoint,
  shortDescription,
  dateInputHelp,
  example,
  lang,
  showKotikuntaPvmInput,
  kotikuntaPvmInputHelp
}) => {
  const paivaAtom = Atom(today())
  const kotikuntaPvmAtom = Atom(today())
  const submitBus = Bacon.Bus()
  const { selectedOrganisaatioP, dbUpdatedP } = selectFromState(stateP)

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    selectedOrganisaatioP,
    paivaAtom,
    kotikuntaPvmAtom,
    (o, p, kkp) =>
      o &&
      p && {
        oppilaitosOid: o.oid,
        paiva: formatISODate(p),
        kotikuntaPvm:
          showKotikuntaPvmInput && !isEqualDate(today(), kkp)
            ? formatISODate(kkp)
            : undefined,
        lang,
        password,
        baseUrl: `/koski/api/raportit${apiEndpoint}`
      }
  )
  const downloadExcelE = submitBus
    .map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError((e) => {
    showError(e)
  })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map((x) => !!x).and(inProgressP.not())

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <PaivaValinta paivaAtom={paivaAtom} ohje={dateInputHelp} />

      {showKotikuntaPvmInput && (
        <PaivaValinta
          paivaAtom={kotikuntaPvmAtom}
          label={t('select-kotikunta-date')}
          ohje={kotikuntaPvmInputHelp}
        />
      )}

      <RaportinLataus
        password={password}
        inProgressP={inProgressP}
        submitEnabledP={submitEnabledP}
        submitBus={submitBus}
        dbUpdatedP={dbUpdatedP}
      />

      <Vinkit>{example}</Vinkit>
    </section>
  )
}
