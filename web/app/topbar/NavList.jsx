import React from 'baret'
import {naviLink} from '../tiedonsiirrot/Tiedonsiirrot'

export default ({location, user}) => {
  if (!user || !user.hasAnyReadAccess) {
    return null
  }
  return (<ul>
    <li>{naviLink('/koski/virkailija', 'Opiskelijat', location.path, '', (path, loc) => loc == path || loc.startsWith('/koski/oppija'))}</li>
    <li>{naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}</li>
    <li>{naviLink('/koski/dokumentaatio', 'Dokumentaatio', location.path, '')}</li>
  </ul>)
}
