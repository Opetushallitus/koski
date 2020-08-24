import React from 'baret'
import {naviLink} from '../tiedonsiirrot/Tiedonsiirrot'
import {withFeatureFlag} from '../components/withFeatureFlag'

export default ({location, user}) => {
  if (!user) {
    return null
  }
  return (<ul>
    {(user.hasAnyReadAccess && !user.isViranomainen) && (
      <li>{naviLink('/koski/virkailija', 'Opiskelijat', location.path, '', (path, loc) => loc == path || loc.startsWith('/koski/oppija'))}</li>
    )}
    {(user.hasAnyReadAccess && !user.isViranomainen) && (
      <li>{naviLink('/koski/tiedonsiirrot', 'Tiedonsiirrot', location.path, '', (path, loc) => loc.startsWith(path))}</li>
    )}
    {user.hasRaportitAccess && (
      <li>{naviLink('/koski/raportit', 'Raportit', location.path, '', (path, loc) => loc.startsWith(path))}</li>
    )}
    <li>{naviLink('/koski/dokumentaatio', 'Dokumentaatio', location.path, '', (path, loc) => loc.startsWith(path))}</li>
    {user.hasKelaUiAccess && ( <KelaNavLinkWithFeatureFlag location={location} />)}
  </ul>)
}

const KelaNavLink = ({location}) => (
  <li>{naviLink('/koski/kela', 'Kela', location.path, '', (path, loc) => loc.startsWith(path))}</li>
)

const KelaNavLinkWithFeatureFlag = withFeatureFlag('kelaUi', KelaNavLink)
