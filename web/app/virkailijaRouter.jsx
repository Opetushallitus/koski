import React from 'react'
import {locationP, parseQuery} from './location.js'
import {oppijaContentP} from './Oppija'
import {UusiOppija} from './uusioppija/UusiOppija'
import {tiedonsiirtolokiContentP} from './Tiedonsiirtoloki'
import {tiedonsiirtovirheetContentP} from './Tiedonsiirtovirheet'
import {tiedonsiirtojenYhteenvetoContentP} from './TiedonsiirtojenYhteenveto'
import {oppijataulukkoContentP} from './Oppijataulukko'
import {validointiContentP} from './Validointi'
import {dokumentaatioContentP} from './dokumentaatio/Dokumentaatio'
import {onlyIfHasReadAccess} from './accessCheck'

export const routeP = locationP.flatMapLatest(({path, queryString, params, hash}) => {
  let oppijaId = (path.match(new RegExp('/koski/oppija/(.*)')) || [])[1]
  let uusiOppijaHetu = parseQuery(hash).hetu
  let uusiOppijaOid = parseQuery(hash).oid
  if (oppijaId) {
    return oppijaContentP(oppijaId)
  } else if (path === '/koski/uusioppija' && (uusiOppijaHetu || uusiOppijaOid)) {
    return { content: (<UusiOppija hetu={uusiOppijaHetu} oid={uusiOppijaOid} />), title: 'Uuden opiskelijan lisäys' }
  } else if (path === '/koski/virkailija') {
    return onlyIfHasReadAccess(oppijataulukkoContentP(queryString, params))
  } else if (path === '/koski/tiedonsiirrot') {
    return tiedonsiirtolokiContentP(queryString)
  } else if (path === '/koski/tiedonsiirrot/virheet') {
    return tiedonsiirtovirheetContentP(queryString)
  } else if (path === '/koski/tiedonsiirrot/yhteenveto') {
    return tiedonsiirtojenYhteenvetoContentP(queryString)
  } else if (path === '/koski/validointi') {
    return validointiContentP(queryString, params)
  } else if (path === '/koski/dokumentaatio') {
    return dokumentaatioContentP()
  }
}).toProperty()

export const contentP = routeP.map('.content')

export const titleKeyP = routeP.map('.title').map(titleKey => titleKey || '')

export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' })