import React from '../util/baret'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import { formatISODate } from '../date/date'
import { AikajaksoValinta, LyhytKuvaus, Vinkit } from './raporttiComponents'
import { selectFromState } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'

export const AikajaksoRaportti = ({
  stateP,
  shortDescription,
  dateInputHelp,
  example,
  massaluovutusKyselymalli,
  lang
}) => {
  const alkuAtom = Atom()
  const loppuAtom = Atom()

  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

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

  return (
    <section>
      <LyhytKuvaus>{shortDescription}</LyhytKuvaus>

      <AikajaksoValinta
        alkuAtom={alkuAtom}
        loppuAtom={loppuAtom}
        ohje={dateInputHelp}
      />

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
