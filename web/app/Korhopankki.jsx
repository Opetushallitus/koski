// eslint-disable-next-line no-undef
import './polyfills/polyfills.js'
import React from 'react'
import ReactDOM from 'react-dom'
import HetuLogin from './korhopankki/HetuLogin'
import Text from './i18n/Text'
import { currentLocation } from './util/location'
__webpack_nonce__ = window.nonce
import(/* webpackChunkName: "styles" */ './style/main.less')

const getParam = (parameter) => {
  return currentLocation().params
    ? currentLocation().params[parameter]
    : undefined
}

const MockUsers = () => (
  <table className="mock-users">
    <thead>
      <tr>
        <th>
          <Text name="Hetu" />
        </th>
        <th>
          <Text name="Nimi" />
        </th>
      </tr>
    </thead>
    <tbody>
      {window.mockUsers.map((user) => (
        <tr key={user.hetu + '-' + user.nimi}>
          <td>{user.hetu}</td>
          <td>{user.nimi}</td>
        </tr>
      ))}
    </tbody>
  </table>
)

ReactDOM.render(
  <div>
    <HetuLogin
      loginUrl={getParam('login')}
      redirectUrl={getParam('redirect')}
    />
    <MockUsers />
  </div>,
  document.getElementById('content')
)
