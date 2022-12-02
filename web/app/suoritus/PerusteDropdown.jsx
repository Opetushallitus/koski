import React from 'baret'
import Http from '../util/http'
import Bacon from 'baconjs'
import Dropdown from '../components/Dropdown'
import { elementWithLoadingIndicator } from '../components/AjaxLoadingIndicator'
import { t } from '../i18n/i18n'

const preferred = ['OPH-1280-2017', '104/011/2014', 'OPH-4958-2020']
const aikuistenLukionOppimääränDiaarinumerot = [
  'OPH-2267-2019',
  'OPH-2263-2019'
]
const onLukionOppiaineenOppimäärä = (koodiarvo) =>
  koodiarvo === 'lukionoppiaineenoppimaara'
const piilotaAikuistenLukionOppimääränDiaarinumerot = (
  diaarinumero,
  suoritustyyppi
) =>
  !onLukionOppiaineenOppimäärä(suoritustyyppi.koodiarvo) ||
  (onLukionOppiaineenOppimäärä(suoritustyyppi.koodiarvo) &&
    !aikuistenLukionOppimääränDiaarinumerot.includes(diaarinumero.koodiarvo))

export const PerusteDropdown = ({ suoritusTyyppiP, perusteAtom }) => {
  const diaarinumerotP = suoritusTyyppiP
    .flatMapLatest((tyyppi) =>
      !tyyppi
        ? Bacon.never()
        : diaarinumerot(tyyppi).flatMapLatest((diaarinumeroList) =>
            diaarinumeroList.filter((diaarinumero) =>
              piilotaAikuistenLukionOppimääränDiaarinumerot(
                diaarinumero,
                tyyppi
              )
            )
          )
    )
    .toProperty()
  const selectedOptionP = Bacon.combineWith(
    diaarinumerotP,
    perusteAtom,
    (options, selected) => options.find((o) => o.koodiarvo == selected)
  )
  const selectOption = (option) => {
    perusteAtom.set(option && option.koodiarvo)
  }

  diaarinumerotP.onValue((options) => {
    const current = perusteAtom.get()
    if (
      !current ||
      (options.length > 0 && !options.map((k) => k.koodiarvo).includes(current))
    ) {
      selectOption(
        options.find((k) => preferred.includes(k.koodiarvo)) || options[0]
      )
    }
  })

  return (
    <span>
      {elementWithLoadingIndicator(
        diaarinumerotP.map((diaarinumerot) =>
          diaarinumerot.length > 1 ? (
            <Dropdown
              data-testid="peruste-dropdown"
              options={diaarinumerotP}
              keyValue={(option) => option.koodiarvo}
              displayValue={(option) => option.koodiarvo + ' ' + t(option.nimi)}
              onSelectionChanged={selectOption}
              selected={selectedOptionP}
            />
          ) : (
            <input type="text" disabled value={perusteAtom}></input>
          )
        )
      )}
    </span>
  )
}

export const diaarinumerot = (suoritusTyyppi) =>
  suoritusTyyppi
    ? Http.cachedGet(
        `/koski/api/tutkinnonperusteet/diaarinumerot/suorituksentyyppi/${suoritusTyyppi.koodiarvo}`
      )
    : []

export const setPeruste = (perusteAtom, suoritusTyyppi) => {
  diaarinumerot(suoritusTyyppi)
    .map((options) => options[0])
    .map('.koodiarvo')
    .onValue((peruste) => {
      const current = perusteAtom.get()
      if (!current || peruste !== current) {
        perusteAtom.set(peruste)
      }
    })
}
