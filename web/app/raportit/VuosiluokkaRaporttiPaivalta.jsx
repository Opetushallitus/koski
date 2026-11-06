import React from '../util/baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import { showError } from '../util/location'
import { formatISODate } from '../date/date'
import { generateRandomPassword } from '../util/password'
import { downloadExcel } from './downloadExcel'
import Dropdown from '../components/Dropdown'
import {
  LyhytKuvaus,
  PaivaValinta,
  RaportinLataus,
  Vinkit
} from './raporttiComponents'
import { kotikuntaPvmParametri, selectFromState, today } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'
import { t } from '../i18n/i18n'

export const VuosiluokkaRaporttiPaivalta = ({
  stateP,
  apiEndpoint,
  shortDescription,
  dateInputHelp,
  example,
  lang,
  showKotikuntaPvmInput,
  kotikuntaPvmInputHelp,
  massaluovutusKyselymalli
}) => {
  const paivaAtom = Atom(today())
  const kotikuntaPvmAtom = Atom(today())
  const vuosiluokkaAtom = Atom('1')
  const submitBus = Bacon.Bus()
  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    selectedOrganisaatioP,
    paivaAtom,
    kotikuntaPvmAtom,
    vuosiluokkaAtom,
    (o, p, kkp, v) =>
      o &&
      p &&
      v &&
      kkp && {
        oppilaitosOid: o.oid,
        paiva: formatISODate(p),
        kotikuntaPvm: showKotikuntaPvmInput ? formatISODate(kkp) : undefined,
        vuosiluokka: v,
        lang,
        password,
        baseUrl: `/koski/api/raportit${apiEndpoint}`
      }
  )

  const massaluovutusParametritP = Bacon.combineWith(
    selectedOrganisaatioP,
    paivaAtom,
    kotikuntaPvmAtom,
    vuosiluokkaAtom,
    (o, p, kkp, v) =>
      o && p && v && (!showKotikuntaPvmInput || kkp)
        ? {
            organisaatioOid: o.oid,
            paiva: formatISODate(p),
            // Valikko antaa vuosiluokan numerona, kysely odottaa merkkijonoa
            vuosiluokka: String(v),
            kotikuntaPvm: kotikuntaPvmParametri(showKotikuntaPvmInput, kkp),
            language: lang
          }
        : null
  )

  const downloadExcelE = submitBus
    .map(downloadExcelP)
    .flatMapLatest(downloadExcel)

  downloadExcelE.onError((e) => {
    showError(e)
  })

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map((x) => !!x).and(inProgressP.not())
  const vuosiluokat = [1, 2, 3, 4, 5, 6, 7, 8, 9]

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <PaivaValinta
        paivaAtom={paivaAtom}
        ohje={dateInputHelp}
        inputId="dateinput-paiva"
      />
      {showKotikuntaPvmInput && (
        <PaivaValinta
          label={t('select-kotikunta-date')}
          paivaAtom={kotikuntaPvmAtom}
          ohje={kotikuntaPvmInputHelp}
          inputId="dateinput-kotikuntapvm"
        />
      )}

      <div className="dropdown-selection parametri vuosiluokka">
        <label>
          <Text name="select-class" />
        </label>
        <VuosiluokkaDropdown
          value={vuosiluokkaAtom}
          vuosiluokat={vuosiluokat}
        />
      </div>

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

const VuosiluokkaDropdown = ({ value, vuosiluokat }) => (
  <div>
    {value.map((v) => (
      <Dropdown
        options={vuosiluokat}
        keyValue={(key) => key}
        displayValue={(dVal) => dVal}
        selected={v}
        onSelectionChanged={(input) => value.set(input)}
      />
    ))}
  </div>
)
