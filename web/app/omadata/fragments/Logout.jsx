import React from 'baret'
import Text from '../../i18n/Text'
import Link from '../../components/Link'
import { userP } from '../../util/user'

export default ({ logoutURL }) => (
  <div>
    <div className='username'>
      <img src='/koski/images/profiili.svg' alt='user-icon' />
      { userP.map(user => user && user.name) }
    </div>
      <div className='logout'>
        <Link href={logoutURL}>
          <Text name='Kirjaudu ulos'/>
        </Link>
      </div>
  </div>
)
