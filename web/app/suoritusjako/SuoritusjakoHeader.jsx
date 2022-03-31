import React from 'baret'
import Text from '../i18n/Text'
import {modelData, modelItems, modelLookup} from '../editor/EditorModel'
import {Varoitukset} from '../util/Varoitukset'
import {HeaderName} from '../omattiedot/header/HeaderName'

export const SuoritusjakoHeader = ({oppija}) => {
  const henkilö = modelLookup(oppija, 'henkilö')
  const varoitukset = modelItems(oppija, 'varoitukset').map(modelData)
  return (
    <header>
      <Varoitukset varoitukset={varoitukset}/>
      <h1 className='header__heading'>
        <Text name='Opinnot'/>
      </h1>
      <div className='header__bottom-row'>
        <HeaderName henkilö={henkilö}/>
      </div>
    </header>
  )
}

SuoritusjakoHeader.displayName = 'SuoritusjakoHeader'
