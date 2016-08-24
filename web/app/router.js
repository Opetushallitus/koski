import { locationP } from './location.js'
import { oppijaHakuContentP } from './OppijaHaku.jsx'

export const contentP = locationP.flatMap(location => {
  if (location.match(new RegExp('/koski/oppija/(.*)'))) {
    return oppijaHakuContentP
  } else if (location === '/koski/uusioppija') {
    return oppijaHakuContentP
  } else if (location === '/koski/') {
    return oppijaHakuContentP
  }
}).toProperty()

export const routeErrorP = contentP.map(content => content ? {} : { httpStatus: 404, comment: 'route not found' })