import React, {fromBacon} from 'baret'
import {modelData, modelItems, modelLookup} from '../../editor/EditorModel'
import DropDown from '../../components/Dropdown'
import Text from '../../i18n/Text'
import {ift} from '../../util/util'

export const OppijaSelector = ({oppijaP, onOppijaChanged}) => {
  return fromBacon(ift(oppijaP.map(oppija => !!oppija), (<div className='oppija-selector'>
    <Text className='oppija-selector__heading' name='Kenen opintoja haluat tarkastella' />
    <DropDown
      options={oppijaP.map(oppija => modelItems(oppija, 'kaikkiHenkilöt'))}
      selected={oppijaP.map(o => modelLookup(o, 'henkilö'))}
      keyValue={o => modelData(o, 'oid')}
      displayValue={o => `${modelData(o, 'etunimet')} ${modelData(o, 'sukunimi')}`}
      onSelectionChanged={onOppijaChanged}
    />
  </div>)))
}
