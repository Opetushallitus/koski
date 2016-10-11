import { locationP } from './location.js'
import { oppijaHakuContentP } from './OppijaHaku.jsx'
import { tiedonsiirtolokiContentP } from './Tiedonsiirtoloki.jsx'
import { tiedonsiirtovirheetContentP } from './Tiedonsiirtovirheet.jsx'
import { tiedonsiirtojenYhteenvetoContentP } from './TiedonsiirtojenYhteenveto.jsx'
import { omatTiedotContentP } from './OmatTiedot.jsx'

export const contentP = locationP.flatMapLatest(({path, queryString}) => {
  if (path.match(new RegExp('/koski/oppija/(.*)'))) {
    return oppijaHakuContentP
  } else if (path === '/koski/uusioppija') {
    return oppijaHakuContentP
  } else if (path === '/koski/') {
    return oppijaHakuContentP
  } else if (path === '/koski/tiedonsiirrot') {
    return tiedonsiirtolokiContentP(queryString)
  } else if (path === '/koski/tiedonsiirrot/virheet') {
    return tiedonsiirtovirheetContentP(queryString)
  } else if (path === '/koski/tiedonsiirrot/yhteenveto') {
    return tiedonsiirtojenYhteenvetoContentP()
  } else if (path === '/koski/omattiedot') {
    return omatTiedotContentP()
  }
}).toProperty()

export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' })