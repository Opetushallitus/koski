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

export const AikajaksoRaporttiTyyppivalinnalla = ({
  stateP,
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
  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

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
