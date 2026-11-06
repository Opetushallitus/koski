import React from '../util/baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import { showError } from '../util/location'
import { formatISODate } from '../date/date'
import { generateRandomPassword } from '../util/password'
import { downloadExcel } from './downloadExcel'
import {
  AikajaksoValinta,
  Listavalinta,
  LyhytKuvaus,
  RaportinLataus,
  Vinkit,
  PaivaValinta
} from './raporttiComponents'
import { kotikuntaPvmParametri, selectFromState, today } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'
import { t } from '../i18n/i18n'

export const AikajaksoRaporttiTyyppivalinnalla = ({
  stateP,
  apiEndpoint,
  shortDescription,
  example,
  lang,
  defaultRaportinTyyppi,
  listavalintaKuvaus,
  raporttiTyypit,
  aikajaksoValintaKuvaus,
  osasuoritustenAikarajausEiKuvaus,
  osasuoritustenAikarajausKylläKuvaus,
  showKotikuntaPvmInput,
  kotikuntaPvmInputHelp,
  massaluovutusKyselymalli
}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const kotikuntaPvmAtom = Atom(today())
  const osasuoritustenAikarajausAtom = Atom(false)
  const raportinTyyppiAtom = Atom(defaultRaportinTyyppi)
  const submitBus = Bacon.Bus()
  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    selectedOrganisaatioP,
    alkuAtom,
    loppuAtom,
    osasuoritustenAikarajausAtom,
    raportinTyyppiAtom,
    kotikuntaPvmAtom,
    (o, a, l, r, tyyppi, kkp) =>
      o &&
      a &&
      l &&
      l.valueOf() >= a.valueOf() &&
      tyyppi &&
      (!showKotikuntaPvmInput || kkp) && {
        oppilaitosOid: o.oid,
        alku: formatISODate(a),
        loppu: formatISODate(l),
        osasuoritustenAikarajaus: r,
        password,
        raportinTyyppi: tyyppi,
        ...(showKotikuntaPvmInput && { kotikuntaPvm: formatISODate(kkp) }),
        lang,
        baseUrl: `/koski/api/raportit${apiEndpoint}`
      }
  )

  const massaluovutusParametritP = Bacon.combineWith(
    selectedOrganisaatioP,
    alkuAtom,
    loppuAtom,
    osasuoritustenAikarajausAtom,
    raportinTyyppiAtom,
    kotikuntaPvmAtom,
    (o, a, l, r, tyyppi, kkp) =>
      o &&
      a &&
      l &&
      l.valueOf() >= a.valueOf() &&
      tyyppi &&
      (!showKotikuntaPvmInput || kkp)
        ? {
            organisaatioOid: o.oid,
            alku: formatISODate(a),
            loppu: formatISODate(l),
            osasuoritustenAikarajaus: r,
            raportinTyyppi: tyyppi,
            kotikuntaPvm: kotikuntaPvmParametri(showKotikuntaPvmInput, kkp),
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

      <Listavalinta
        label={listavalintaKuvaus}
        atom={raportinTyyppiAtom}
        options={Object.values(raporttiTyypit).map((v) => {
          return {
            key: v.key,
            value: <Text name={v.name} />
          }
        })}
      />

      <AikajaksoValinta alkuAtom={alkuAtom} loppuAtom={loppuAtom} />

      <Listavalinta
        label={aikajaksoValintaKuvaus}
        atom={osasuoritustenAikarajausAtom}
        options={[
          {
            key: false,
            value: <Text name={osasuoritustenAikarajausEiKuvaus} />
          },
          {
            key: true,
            value: <Text name={osasuoritustenAikarajausKylläKuvaus} />
          }
        ]}
      />

      {showKotikuntaPvmInput && (
        <PaivaValinta
          label={t('select-kotikunta-date')}
          paivaAtom={kotikuntaPvmAtom}
          ohje={kotikuntaPvmInputHelp}
          inputId="dateinput-kotikuntapvm"
        />
      )}

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
