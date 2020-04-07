import React from 'baret'
import Dropdown from '../../components/Dropdown'
import {modelData} from '../../editor/EditorModel'
import {t} from '../../i18n/i18n'

export const HuollettavaDropdown = ({oppija, oppijaSelectionBus}) => {
  const kirjautunutHenkilo = modelData(oppija, 'userHenkilö')
  const valittuHenkilo = modelData(oppija, 'henkilö')
  const huollettavat = modelData(oppija, 'huollettavat')

  const options = huollettavat.concat(kirjautunutHenkilo)
    .filter(h => h.oid != valittuHenkilo.oid)
    .sort(aakkosjarjestys)

  return (
    options.length > 1 &&
    <div className='header__oppijanvalitsin'>
      <h2 className='header__heading'> {t('Kenen opintoja haluat tarkastella?')}
      </h2>
      <Dropdown
        options={options}
        keyValue={option => option.oid}
        displayValue={option => option.etunimet + ' ' + option.sukunimi + (option.oid ? '' : ` (${t('Ei opintoja')})`)}
        onSelectionChanged={henkilo => oppijaSelectionBus.push({params: {oid: henkilo.oid}})}
        selected={valittuHenkilo}
        className='huoltajan__valitsin'
        isOptionEnabled={option => option.oid}
      />
    </div>
  )
}

const aakkosjarjestys = (a, b) => a.etunimet.localeCompare(b.etunimet)
