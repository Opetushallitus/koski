import React from 'baret'
import Text from '../i18n/Text'
import { ChangeLang } from '../components/ChangeLang'

export default () => (
  <div className="header">
    <div className="title">
      <img src="/koski/images/opintopolku_logo.svg" alt="" />
      <h1>
        <Text name="Oma Opintopolku" />
      </h1>
    </div>

    <div className="lang">
      <ChangeLang />
    </div>
  </div>
)
