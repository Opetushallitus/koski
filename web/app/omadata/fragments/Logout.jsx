import React from 'baret'
import Text from '../../i18n/Text'
import { userP } from '../../util/user'

const Logout = ({ logoutURL }) => (
  <div>
    <div className='username'>
      <img src='/koski/images/profiili.svg' alt='user-icon' />
      { userP.map(user => user && user.name) }
    </div>
      <div className='logout'>
        <a href={logoutURL}>
          <Text name='Kirjaudu ulos'/>
        </a>
      </div>
  </div>
)

Logout.displayName = 'Logout'

export default Logout
