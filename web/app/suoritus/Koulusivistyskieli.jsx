import React from 'react'
import { Infobox } from '../components/Infobox'
import Text from '../i18n/Text'

export const KoulusivistyskieliPropertyTitle = () => (
  <>
    <Text name="Koulusivistyskieli" />
    <Infobox>
      <Text
        className="bold"
        name="Koulusivistyskieli määräytyy seuraavien suoritusten perusteella"
      />
      <br />
      <ol>
        <li>
          <Text name="Peruskoulun tai lukion päättötodistuksessa hyväksytty arvosana äidinkielenä opiskellusta suomen tai ruotsin kielestä." />
          <br />
        </li>
        <li>
          <Text name="Ylioppilastutkinnossa hyväksytty koe äidinkielessä tai vähintään arvosana magna cum laude approbatur suomi toisena kielenä tai ruotsi toisena kielenä -kokeessa." />
        </li>
      </ol>
    </Infobox>
  </>
)
