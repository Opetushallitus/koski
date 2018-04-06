import React from 'baret'
import Text from '../i18n/Text'
import {ISO2FinnishDate} from '../date/date'
import {modelData, modelLookup, modelTitle} from '../editor/EditorModel'

export const SuoritusjakoHeader = ({oppija}) => {
  const henkilö = modelLookup(oppija, 'henkilö')
  const nimi = <p>{`${modelData(henkilö, 'etunimet')} ${modelData(henkilö, 'sukunimi')}`}</p>
  const syntymäaika = modelTitle(henkilö, 'syntymäaika') &&
    <p className='syntynyt'>
      <Text name='syntynyt'/>
      <span> {ISO2FinnishDate(modelTitle(henkilö, 'syntymäaika'))}</span>
    </p>
  return (
    <header>
      <h2 className='header__heading'>
        <Text name='Opinnot'/>
      </h2>
      <div className='header__bottom-row'>
        <div className='header__name'>
          {nimi}
          {syntymäaika}
        </div>
      </div>
    </header>
  )
}
