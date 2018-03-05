import React from 'baret'
import Bacon from 'baconjs'
import R from 'ramda'
import Text from '../i18n/Text'
import {modelData} from '../editor/EditorModel'
import Http from '../util/http'
import {laajuusNumberToString} from '../util/format'

export const fetchLaajuudet = (suoritus, groupIds) => {
  let diaarinumero = modelData(suoritus, 'koulutusmoduuli.perusteenDiaarinumero')
  let suoritustapa = modelData(suoritus, 'suoritustapa.koodiarvo')
  const ids = groupIds.filter(i => i !== '999999').join(',')

  if (suoritustapa === undefined) {
    return Bacon.constant([])
  }

  let map404ToEmpty = { errorMapper: (e) => e.httpStatus === 404 ? [] : Bacon.Error(e) }
  return Http.cachedGet(
    `/koski/api/tutkinnonperusteet/tutkinnonosaryhma/laajuus/${encodeURIComponent(diaarinumero)}/${encodeURIComponent(suoritustapa)}/${encodeURIComponent(ids)}`,
    map404ToEmpty
  )
}

const isEmptyObject = obj => Object.keys(obj).length === 0 && obj.constructor === Object
const rangeExists = l => l && !isEmptyObject(l) && (l.min !== undefined || l.max !== undefined)

const laajuusRange = (l) => {
  if (!rangeExists(l)) {
    return null
  }
  else if (l.min !== undefined && l.max !== undefined) {
    if (l.min === l.max) {
      return l.max.toString()
    }
    else {
      return l.min.toString() + '–' + l.max.toString()
    }
  }
  else {
    return (l.min === undefined
      ? ('-'+l.max.toString())
      : (l.min.toString()+'-')
    )
  }
}

export const YhteensäSuoritettu = ({osasuoritukset, laajuusP, laajuusYksikkö=null}) => {
  const arvioidutSuoritukset = osasuoritukset.filter(s => !!modelData(s, 'arviointi'))
  const laajuudetYhteensä = R.sum(R.map(item => modelData(item, 'koulutusmoduuli.laajuus.arvo') || 0, arvioidutSuoritukset))

  return (
    <div>
      <Text name="Yhteensä"/>
      {' '}
      <span className="laajuudet-yhteensä">{laajuusNumberToString(laajuudetYhteensä)}</span>
      <span className="separator">{laajuusP.map(v => rangeExists(v) ? ' / ' : null)}</span>
      <span className="laajuus-range">{laajuusP.map(v => laajuusRange(v))}</span>
      {' '}
      {laajuusYksikkö}
    </div>
  )
}
