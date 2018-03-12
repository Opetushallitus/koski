import React from 'react'
import Text from '../i18n/Text'
import {modelData} from '../editor/EditorModel'

export const Header = ({henkilö}) => {
  const nimi = `${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`
  const syntymäaika = modelData(henkilö, 'syntymäaika')
    ? <span><Text name='syntynyt'/> {modelData(henkilö, 'syntymäaika')}</span>
    : ''

  return (
    <header>
      <h2 className='header__heading'>
        <Text name='Opintoni'/>
      </h2>
      <div className='header__caption'>
        <p>
          <Text name='Opintoni ingressi'/>
        </p>
        <button className='text'>
          <Text name='Mitkä tiedot palvelussa näkyvät?'/>
        </button>
      </div>
      <p className='header__name'>
        {nimi}
        {syntymäaika}
      </p>
    </header>
  )
}
