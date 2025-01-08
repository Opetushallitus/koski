import React from 'baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
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
import { selectFromState, today } from './raporttiUtils'
import { t } from '../i18n/i18n'

export const osasuoritusTypes = {
  TUTKINNON_OSA: 'tutkinnon osat',
  KURSSI: 'kurssisuoritukset',
  OPINNOT: 'opinnot'
}

const KaikkiSuorituksetLabel = ({ osasuoritusType }) => {
  switch (osasuoritusType) {
    case osasuoritusTypes.TUTKINNON_OSA:
      return (
        <Text name="Raportille valitaan kaikki tutkinnon osat riippumatta niiden suoritusajankohdasta" />
      )
    case osasuoritusTypes.KURSSI:
      return (
        <Text name="Raportille valitaan kaikki kurssisuoritukset riippumatta niiden suoritusajankohdasta" />
      )
    case osasuoritusTypes.OPINNOT:
      return (
        <Text name="Raportille valitaan kaikki opinnot riippumatta niiden suoritusajankohdasta" />
      )
  }
}

const AikarajatutSuorituksetLabel = ({ osasuoritusType }) => {
  switch (osasuoritusType) {
    case osasuoritusTypes.TUTKINNON_OSA:
      return (
        <Text name="Raportille valitaan vain sellaiset tutkinnon osat, joiden arviointipäivä osuu yllä määritellylle aikajaksolle" />
      )
    case osasuoritusTypes.KURSSI:
      return (
        <Text name="Raportille valitaan vain sellaiset kurssisuoritukset, joiden arviointipäivä osuu yllä määritellylle aikajaksolle" />
      )
    case osasuoritusTypes.OPINNOT:
      return (
        <Text name="Raportille valitaan vain sellaiset opinnot, joiden arviointipäivä osuu yllä määritellylle aikajaksolle" />
      )
  }
}

export const AikajaksoRaporttiAikarajauksella = ({
  stateP,
  apiEndpoint,
  shortDescription,
  showKotikuntaPvmInput,
  kotikuntaPvmInputHelp,
  example,
  osasuoritusType = osasuoritusTypes.TUTKINNON_OSA,
  lang
}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const kotikuntaPvmAtom = Atom(today())
  const osasuoritustenAikarajausAtom = Atom(false)
  const submitBus = Bacon.Bus()
  const { selectedOrganisaatioP, dbUpdatedP } = selectFromState(stateP)

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    selectedOrganisaatioP,
    alkuAtom,
    loppuAtom,
    osasuoritustenAikarajausAtom,
    kotikuntaPvmAtom,
    (o, a, l, r, kkp) =>
      o &&
      a &&
      l &&
      l.valueOf() >= a.valueOf() &&
      (!showKotikuntaPvmInput || kkp) && {
        oppilaitosOid: o.oid,
        alku: formatISODate(a),
        loppu: formatISODate(l),
        kotikuntaPvm: formatISODate(kkp),
        osasuoritustenAikarajaus: r,
        lang,
        password,
        baseUrl: `/koski/api/raportit${apiEndpoint}`
      }
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

      <AikajaksoValinta alkuAtom={alkuAtom} loppuAtom={loppuAtom} />

      <Listavalinta
        seamless
        atom={osasuoritustenAikarajausAtom}
        options={[
          {
            key: false,
            value: <KaikkiSuorituksetLabel osasuoritusType={osasuoritusType} />
          },
          {
            key: true,
            value: (
              <AikarajatutSuorituksetLabel osasuoritusType={osasuoritusType} />
            )
          }
        ]}
      />

      {showKotikuntaPvmInput && (
        <PaivaValinta
          label={t('select-kotikunta-date')}
          paivaAtom={kotikuntaPvmAtom}
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
