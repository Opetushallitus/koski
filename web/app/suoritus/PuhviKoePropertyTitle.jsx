import React from 'react'
import {Infobox} from '../components/Infobox'
import Text from '../i18n/Text'

export const PuhviKoePropertyTitle = ({}) => (
  <>
    <Text name='Puhvi-koe'/>
    <Infobox>
      <Text className='bold' name='Puhvi-koe'/><br/>
      <Text name='Puheviestintätaitojen päättökoe eli Puhvi-koe on Opetushallituksen vuosittain laatima toisen asteen opiskelijoiden puheviestintätaitojen päättökoe lukiolaisille ja ammatillisen peruskoulutuksen opiskelijoille.'/>
    </Infobox>
  </>
)

PuhviKoePropertyTitle.displayName = 'PuhviKoePropertyTitle'
