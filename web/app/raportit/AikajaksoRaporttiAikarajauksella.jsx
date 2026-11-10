import React from '../util/baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import { formatISODate } from '../date/date'
import {
  AikajaksoValinta,
  Listavalinta,
  LyhytKuvaus,
  Vinkit,
  PaivaValinta
} from './raporttiComponents'
import { kotikuntaPvmParametri, selectFromState, today } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'
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
  shortDescription,
  showKotikuntaPvmInput,
  kotikuntaPvmInputHelp,
  example,
  osasuoritusType = osasuoritusTypes.TUTKINNON_OSA,
  hideOsasuoritustenAikarajaus,
  massaluovutusKyselymalli,
  lang
}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()
  const kotikuntaPvmAtom = Atom(today())
  const osasuoritustenAikarajausAtom = Atom(false)
  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

  const massaluovutusParametritP = Bacon.combineWith(
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
      (!showKotikuntaPvmInput || kkp)
        ? {
            organisaatioOid: o.oid,
            alku: formatISODate(a),
            loppu: formatISODate(l),
            osasuoritustenAikarajaus: r,
            kotikuntaPvm: kotikuntaPvmParametri(showKotikuntaPvmInput, kkp),
            language: lang
          }
        : null
  )

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <AikajaksoValinta alkuAtom={alkuAtom} loppuAtom={loppuAtom} />

      {!hideOsasuoritustenAikarajaus && (
        <Listavalinta
          seamless
          atom={osasuoritustenAikarajausAtom}
          options={[
            {
              key: false,
              value: (
                <KaikkiSuorituksetLabel osasuoritusType={osasuoritusType} />
              )
            },
            {
              key: true,
              value: (
                <AikarajatutSuorituksetLabel
                  osasuoritusType={osasuoritusType}
                />
              )
            }
          ]}
        />
      )}

      {showKotikuntaPvmInput && (
        <PaivaValinta
          label={t('select-kotikunta-date')}
          paivaAtom={kotikuntaPvmAtom}
          ohje={kotikuntaPvmInputHelp}
          inputId="dateinput-kotikuntapvm"
        />
      )}

      {Bacon.combineWith(
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
      )}

      <Vinkit>{example}</Vinkit>
    </section>
  )
}
