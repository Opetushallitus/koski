import React from 'baret'
import * as R from 'ramda'

import { modelData, modelLookup, pushRemoval } from '../editor/EditorModel'
import { PropertiesEditor } from '../editor/PropertiesEditor'
import { suoritusProperties } from '../suoritus/SuoritustaulukkoCommon'
import { ObjectModel, ObjectModelProperty } from '../types/EditorModels.js'
import { OsasuoritusEditorModel } from '../types/OsasuoritusEditorModel'
import { doActionWhileMounted } from '../util/util'
import { saveOrganizationalPreference } from '../virkailija/organizationalPreferences'
import { MuuKuinSäänneltySuoritustaulukko } from './MuuKuinSäänneltySuoritustaulukko'

type MuuKuinSäänneltyOsasuoritusEditorProps = {
  model: OsasuoritusEditorModel
  onExpand?: () => void
  expanded: boolean
  nestedLevel: number
  columns: any[] // TODO
}

type MuuKuinSäänneltyOsasuoritusEditorState = {
  changed: boolean
}

export class MuuKuinSäänneltyOsasuoritusEditor extends React.Component<
  MuuKuinSäänneltyOsasuoritusEditorProps,
  MuuKuinSäänneltyOsasuoritusEditorState
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
      koulutusmoduuliModel.value.classes.includes('paikallinenkoulutusmoduuli')
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

    return (
      <tbody className={'muks-osasuoritus'}>
        <tr>
          {columns.map((column) =>
            column.renderData({
              model,
              expanded,
              onExpand,
              showTila: true,
              hasProperties: true
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
              <MuuKuinSäänneltySuoritustaulukko
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
    nextProps: MuuKuinSäänneltyOsasuoritusEditorProps
  ) {
    const currentData = modelData(this.props.model)
    const newData = modelData(nextProps.model)

    if (!R.equals(currentData, newData)) this.setState({ changed: true })
  }
}
