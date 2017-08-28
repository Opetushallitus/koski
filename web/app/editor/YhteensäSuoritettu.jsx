import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import Text from '../Text.jsx'
import {modelData} from './EditorModel'
import Http from '../http'

const laajuus = (suoritus, group) => {
  let diaarinumero = modelData(suoritus, 'koulutusmoduuli.perusteenDiaarinumero')
  let suoritustapa = modelData(suoritus, 'suoritustapa.koodiarvo')

  if (suoritustapa === undefined || group === '999999') {
    return Bacon.constant(null)
  }

  let map404ToEmpty = { errorMapper: (e) => e.httpStatus === 404 ? {min: null, max: null} : Bacon.Error(e) }
  return (Http
    .cachedGet(`/koski/api/tutkinnonperusteet/tutkinnonosaryhma/laajuus/${encodeURIComponent(diaarinumero)}/${encodeURIComponent(suoritustapa)}/${encodeURIComponent(group)}`, map404ToEmpty)
    .map(v => v.min === undefined ? {min: null, max: null} : v)
    .filter(e => e.statusCode !== 404))
}

const laajuusRange = (l) => {
  if (!l || (l.min === null && l.max === null)) {
    return null
  }
  else if (l.min !== null && l.max !== null) {
    if (l.min === l.max) {
      return l.max.toString()
    }
    else {
      return l.min.toString() + '–' + l.max.toString()
    }
  }
  else {
    return (l.min !== null ? l.max : l.min).toString()
  }
}

export const YhteensäSuoritettu = ({suoritus, osasuoritukset, group, laajuusYksikkö=null}) => {
  const laajuudetYhteensä = R.sum(R.map(item => modelData(item, 'koulutusmoduuli.laajuus.arvo') || 0, osasuoritukset))
  let laajuudetP = laajuus(suoritus, group)

  return (
    <div>
      <Text name="Yhteensä"/>
      {' '}
      <span className="laajuudet-yhteensä">{laajuudetYhteensä}</span>
      <span className="separator">{laajuudetP.map(v => (!v || v.min === null || v.max === null) ? null : ' / ')}</span>
      <span className="laajuus-range">{laajuudetP.map(v => laajuusRange(v))}</span>
      {' '}
      {laajuusYksikkö}
    </div>
  )
}