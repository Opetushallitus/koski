import React from 'baret'
import { UserInfo } from './UserInfo'
import Link from '../components/Link'
import Text from '../i18n/Text'
import NavList from './NavList'
import InvalidationNotification from '../components/InvalidationNotification'

export default ({ location, user, titleKey }) => {
  return (
    <header
      id="topbar"
      className={'local' + (user && user.isViranomainen ? ' viranomainen' : '')}
    >
      <div id="logo">
        <Text name="Opintopolku.fi" />
      </div>
      <h1>
        <Link href="/koski/">
          <Text name="Koski" />
        </Link>
        {titleKey ? (
          <span>
            {' - '}
            <Text name={titleKey} />
          </span>
        ) : null}
      </h1>
      <UserInfo user={user} />
      {user !== null && (
        <div className="topbarnav">
          <NavList location={location} user={user} />
          <InvalidationNotification />
        </div>
      )}
    </header>
  )
}
