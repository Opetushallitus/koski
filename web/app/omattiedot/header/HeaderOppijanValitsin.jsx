import React from 'baret'
import Dropdown from '../../components/Dropdown'
import {modelData, modelItems} from '../../editor/EditorModel'
import Atom from 'bacon.atom'
import {t} from '../../i18n/i18n'

const valitseOppija = (oid) => window.location = `/koski/omattiedot?code=${oid}`

export const HeaderOppijanValitsin = ({oppija}) => {
  let huollettavat = modelItems(oppija, 'huollettavat')

  let userHenkilo = modelData(oppija, 'userHenkilö')
  let haluttuHenkilo = modelData(oppija, 'henkilö')

  let to_select = userHenkilo
  let options = huollettavat.map(huollettava => {
    let etunimet = modelData(huollettava, 'etunimet')
    let sukunimi = modelData(huollettava, 'sukunimi')
    let oid = modelData(huollettava, 'oid')

    if (oid === haluttuHenkilo.oid) {
      to_select = haluttuHenkilo
    }

    return {etunimet: etunimet, sukunimi: sukunimi, oid: oid}
  })

  options.unshift(userHenkilo)

  let selected = Atom(to_select)

  let onChange = (value) => {selected.set(value);valitseOppija(value.oid)}
  return (
   options.length > 1 &&
    <div className='header__oppijanvalitsin'>
      <h2 className='header__heading'> {t('Kenen opintoja haluat tarkastella?')}
      </h2>
      <Dropdown
              options={options}
              keyValue={option => option.oid}
              displayValue={option => option.etunimet + ' ' + option.sukunimi}
              onSelectionChanged={option => onChange(option)}
              selected={selected}
              className='huoltajan__valitsin'
              enableFilter={true}
            />
    </div>
  )
}
