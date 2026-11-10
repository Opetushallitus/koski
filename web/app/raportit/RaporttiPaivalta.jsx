import React from '../util/baret'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import { formatISODate } from '../date/date'
import { LyhytKuvaus, PaivaValinta, Vinkit } from './raporttiComponents'
import { kotikuntaPvmParametri, selectFromState, today } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'
import { t } from '../i18n/i18n'

export const RaporttiPaivalta = ({
  stateP,
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
  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

  const massaluovutusParametritP = Bacon.combineWith(
    selectedOrganisaatioP,
    paivaAtom,
    kotikuntaPvmAtom,
    (o, p, kkp) =>
      o && p && (!showKotikuntaPvmInput || kkp)
        ? {
            organisaatioOid: o.oid,
            paiva: formatISODate(p),
            kotikuntaPvm: kotikuntaPvmParametri(showKotikuntaPvmInput, kkp),
            language: lang
          }
        : null
  )

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
          paivaAtom={kotikuntaPvmAtom}
          label={t('select-kotikunta-date')}
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
