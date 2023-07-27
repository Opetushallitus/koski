import React from 'baret'
import * as R from 'ramda'

import { modelData, modelLookup, pushRemoval } from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { suoritusProperties } from '../suoritus/SuoritustaulukkoCommon'
import { VapaanSivistystyonSuoritustaulukko } from './VapaanSivistystyonSuoritustaulukko'
import { saveOrganizationalPreference } from '../virkailija/organizationalPreferences'
import { doActionWhileMounted } from '../util/util'
import { ObjectModel, ObjectModelProperty } from '../types/EditorModels.js'
import { OsasuoritusEditorModel } from '../types/OsasuoritusEditorModel'
import { intersects } from '../util/array'

type VapaanSivistystyonOsasuoritusEditorProps = {
  model: OsasuoritusEditorModel
  onExpand?: () => void
  expanded: boolean
  nestedLevel: number
  columns: any[] // TODO
}

type VapaanSivistystyonOsasuoritusEditorState = {
  changed: boolean
}

const tallennettavatKoulutusmoduuliClasses = [
  'paikallinenkoulutusmoduuliilmankuvausta',
  'paikallinenkoulutusmoduuli'
]

export class VapaanSivistystyonOsasuoritusEditor extends React.Component<
  VapaanSivistystyonOsasuoritusEditorProps,
  VapaanSivistystyonOsasuoritusEditorState
> {
  saveChangedPreferences() {
    if (!this.state || !this.state.changed) return null

    const { model } = this.props

    const koulutusmoduuliData = modelData(model).koulutusmoduuli
    const organisaatioOid = modelData(model.context.toimipiste).oid
    const key = koulutusmoduuliData.tunniste.koodiarvo
    const koulutusmoduuliModel = modelLookup(
      model,
      'koulutusmoduuli'
    ) as ObjectModel
    const moduulinTyyppi = koulutusmoduuliModel.value.classes[0]

    if (
      intersects(
        koulutusmoduuliModel.value.classes,
        tallennettavatKoulutusmoduuliClasses
      )
    ) {
      saveOrganizationalPreference(
        organisaatioOid,
        moduulinTyyppi,
        key,
        koulutusmoduuliData
      )
    }
  }

  render() {
    const { model, onExpand, expanded, nestedLevel, columns } = this.props

    const editableProperties: ObjectModelProperty[] = suoritusProperties(
      model
    ).filter((p: ObjectModelProperty) => p.key !== 'osasuoritukset')
    const osasuoritukset = modelLookup(model, 'osasuoritukset')

    const canExpand =
      onExpand !== undefined &&
      model.value.classes.find((c) =>
        c.includes('vstkotoutumiskoulutuksenohjauksensuoritus2022')
      ) === undefined

    return (
      <tbody className={'vst-osasuoritus'}>
        <tr>
          {columns.map((column) =>
            column.renderData({
              model,
              expanded,
              onExpand,
              showTila: true,
              hasProperties: canExpand
            })
          )}
          {model.context.edit && (
            <td className="remove">
              <a className="remove-value" onClick={() => pushRemoval(model)} />
            </td>
          )}
        </tr>
        {expanded && editableProperties.length > 0 && (
          <tr className="details" key="details">
            <td colSpan={4}>
              <PropertiesEditor model={model} properties={editableProperties} />
            </td>
          </tr>
        )}
        {expanded && (
          <tr className="osasuoritukset">
            <td colSpan={4}>
              <VapaanSivistystyonSuoritustaulukko
                parentSuoritus={model}
                suorituksetModel={osasuoritukset}
                nestedLevel={nestedLevel}
              />
            </td>
          </tr>
        )}
        {model.context.edit &&
          doActionWhileMounted(model.context.saveChangesBus, () => {
            this.saveChangedPreferences()
          })}
      </tbody>
    )
  }

  UNSAFE_componentWillReceiveProps(
    nextProps: VapaanSivistystyonOsasuoritusEditorProps
  ) {
    const currentData = modelData(this.props.model)
    const newData = modelData(nextProps.model)

    if (!R.equals(currentData, newData)) this.setState({ changed: true })
  }
}
