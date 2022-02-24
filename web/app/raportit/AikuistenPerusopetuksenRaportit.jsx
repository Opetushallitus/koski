import React from 'baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import {showError} from '../util/location'
import {formatISODate} from '../date/date'
import {generateRandomPassword} from '../util/password'
import {downloadExcel} from './downloadExcel'
import { AikajaksoValinta, Listavalinta, LyhytKuvaus, RaportinLataus, Vinkit } from './raporttiComponents'
import { selectFromState } from './raporttiUtils'

const reportTypes = {
  alkuvaihe: 'alkuvaihe',
  päättövaihe: 'päättövaihe',
  oppiaineenoppimäärä: 'oppiaineenoppimäärä'
}

export const AikuistenPerusopetuksenRaportit = ({stateP, apiEndpoint, shortDescription, example, lang}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const osasuoritustenAikarajausAtom = Atom(false)
  const raportinTyyppiAtom = Atom(reportTypes.alkuvaihe)
  const submitBus = Bacon.Bus()
  const { selectedOrganisaatioP, dbUpdatedP } = selectFromState(stateP)

  const password = generateRandomPassword()

  const downloadExcelP = Bacon.combineWith(
    selectedOrganisaatioP, alkuAtom, loppuAtom, osasuoritustenAikarajausAtom, raportinTyyppiAtom,
    (o, a, l, r, t) => o && a && l && (l.valueOf() >= a.valueOf()) && t && {
      oppilaitosOid: o.oid,
      alku: formatISODate(a),
      loppu: formatISODate(l),
      osasuoritustenAikarajaus: r,
      password,
      raportinTyyppi: t,
      lang: lang,
      baseUrl: `/koski/api/raportit${apiEndpoint}`
    })

  const downloadExcelE = submitBus.map(downloadExcelP).flatMapLatest(downloadExcel)

  downloadExcelE.onError(e => showError(e))

  const inProgressP = submitBus.awaiting(downloadExcelE.mapError())
  const submitEnabledP = downloadExcelP.map(x => !!x).and(inProgressP.not())

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <Listavalinta
        label="suorituksentyyppivalinta-help"
        atom={raportinTyyppiAtom}
        options={[
          { key: reportTypes.alkuvaihe, value: <Text name="Alkuvaihe" /> },
          { key: reportTypes.päättövaihe, value: <Text name="Päättövaihe" /> },
          { key: reportTypes.oppiaineenoppimäärä, value: <Text name="Oppiaineen oppimäärä (ns. aineopiskelijat)" /> }
        ]}
      />

      <AikajaksoValinta
        alkuAtom={alkuAtom}
        loppuAtom={loppuAtom}
      />

      <Listavalinta
        label="aikuistenperusopetuksen-raportti-osasuoritusten-aikavaraus-help"
        atom={osasuoritustenAikarajausAtom}
        options={[
          { key: false, value: <Text name="Raportille valitaan kaikki kurssisuoritukset riippumatta niiden suoritusajankohdasta" /> },
          { key: true, value: <Text name="Raportille valitaan vain sellaiset kurssit, joiden arviointipäivä osuu yllä määritellylle aikajaksolle" /> }
        ]}
      />

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
