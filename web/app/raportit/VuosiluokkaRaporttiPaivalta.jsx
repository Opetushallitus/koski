import React from '../util/baret'
import Text from '../i18n/Text'
import Bacon from 'baconjs'
import Atom from '../util/bacon.atom'
import { formatISODate } from '../date/date'
import Dropdown from '../components/Dropdown'
import { LyhytKuvaus, PaivaValinta, Vinkit } from './raporttiComponents'
import { kotikuntaPvmParametri, selectFromState, today } from './raporttiUtils'
import { MassaluovutusRaportinLataus } from '../components-v2/raportit/MassaluovutusRaportinLataus'
import { t } from '../i18n/i18n'

export const VuosiluokkaRaporttiPaivalta = ({
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
  const vuosiluokkaAtom = Atom('1')
  const { selectedOrganisaatioP, dbUpdatedP, organisaatioNimetP } =
    selectFromState(stateP)

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
