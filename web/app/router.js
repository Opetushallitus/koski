import { locationP } from './location.js'
import { oppijaHakuContentP } from './OppijaHaku.jsx'
import { tiedonsiirtolokiContentP } from './Tiedonsiirtoloki.jsx'
import { tiedonsiirtovirheetContentP } from './Tiedonsiirtovirheet.jsx'
import { omatTiedotContentP } from './OmatTiedot.jsx'

export const contentP = locationP.flatMapLatest(location => {
  if (location.match(new RegExp('/koski/oppija/(.*)'))) {
    return oppijaHakuContentP
  } else if (location === '/koski/uusioppija') {
    return oppijaHakuContentP
  } else if (location === '/koski/') {
    return oppijaHakuContentP
  } else if (location === '/koski/tiedonsiirrot') {
    return tiedonsiirtolokiContentP()
  } else if (location === '/koski/tiedonsiirrot/virheet') {
    return tiedonsiirtovirheetContentP()
  } else if (location === '/koski/omattiedot') {
    return omatTiedotContentP
  }
}).toProperty()

export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' })