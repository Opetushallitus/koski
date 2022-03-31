import React from 'react'
import {t} from '../../i18n/i18n'
import Text from '../../i18n/Text'

export const PuuttuvatTiedot = () => (
  <div className='puuttuvat-tiedot__info'>
    <p><b>{t('Huomioithan, että Koski-palvelussa ei pystytä näyttämään seuraavia tietoja:')}</b></p>
    <ul>
      <li>
        <Text name='Korkeakoulututkintoja ennen vuotta 1995. Tässä voi olla korkeakoulukohtaisia poikkeuksia.'/>
      </li>
      <li>
        <Text name='Ennen vuotta 1990 suoritettuja ylioppilastutkintoja.'/>
      </li>
      <li>
        <Text name='Ennen vuoden 2018 tammikuuta suoritettuja peruskoulun, lukion tai ammattikoulun suorituksia ja opiskeluoikeuksia.'/>
      </li>
    </ul>
  </div>
)

PuuttuvatTiedot.displayName = 'PuuttuvatTiedot'
