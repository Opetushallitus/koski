import React from 'react'
import {locationP, parseQuery} from '../util/location.js'
import {oppijaContentP} from './VirkailijaOppijaView'
import {UusiOppija} from '../uusioppija/UusiOppija'
import {tiedonsiirtolokiContentP} from '../tiedonsiirrot/Tiedonsiirtoloki'
import {tiedonsiirtovirheetContentP} from '../tiedonsiirrot/Tiedonsiirtovirheet'
import {tiedonsiirtojenYhteenvetoContentP} from '../tiedonsiirrot/TiedonsiirtojenYhteenveto'
import {oppijataulukkoContentP} from './Oppijataulukko'
import {dokumentaatioYleistäP, dokumentaatioTietomalliP, dokumentaatioKoodistotP, dokumentaatioOppilashallintojärjestelmätP, dokumentaatioLuovutuspalveluP, dokumentaatioPalveluväyläOmadataP} from '../dokumentaatio/Dokumentaatio'
import {onlyIfHasReadAccess} from './accessCheck'
import {raportitContentP} from '../raportit/Raportit'
import {kelaVirkailijaP} from '../kela/KelaVirkailija'

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
  } else if (path === '/koski/raportit') {
    return raportitContentP()
  } else if (path === '/koski/dokumentaatio') {
    return dokumentaatioYleistäP()
  } else if (path === '/koski/dokumentaatio/tietomalli') {
    return dokumentaatioTietomalliP()
  } else if (path === '/koski/dokumentaatio/koodistot') {
    return dokumentaatioKoodistotP()
  } else if (path === '/koski/dokumentaatio/rajapinnat/oppilashallintojarjestelmat') {
    return dokumentaatioOppilashallintojärjestelmätP()
  } else if (path === '/koski/dokumentaatio/rajapinnat/luovutuspalvelu') {
    return dokumentaatioLuovutuspalveluP()
  } else if (path === '/koski/dokumentaatio/rajapinnat/palveluvayla-omadata') {
    return dokumentaatioPalveluväyläOmadataP()
  } else if (path.includes('koski/kela')) {
    const hetu = (path.match(new RegExp('/koski/kela/(.*)')) || [])[1]
    return kelaVirkailijaP(hetu)
  }
}).toProperty()

export const contentP = routeP.map('.content')

export const titleKeyP = routeP.map('.title').map(titleKey => titleKey || '')

export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' })
