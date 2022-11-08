import React from 'baret'
import { Editor } from '../editor/Editor'
import { equals } from 'ramda'
import * as L from 'partial.lenses'
import {
  hasModelProperty,
  lensedModel,
  modelData,
  modelLookup,
  modelSetValue
} from '../editor/EditorModel'
import { sortLanguages } from '../util/sorting'
import { saveOrganizationalPreference } from '../virkailija/organizationalPreferences'
import { doActionWhileMounted } from '../util/util'
import {
  isKieliaine,
  isPaikallinen,
  koulutusModuuliprototypes
} from '../suoritus/Koulutusmoduuli'
import { t } from '../i18n/i18n'

// TODO: TOR-1685

export class EuropeanSchoolOfHelsinkiOppiaineEditor extends React.Component {
  render() {
    const { oppiaine, showExpand, onExpand, expanded, uusiOppiaineenSuoritus } =
      this.props

    return (
      <span>
        {oppiaine.context.edit && isPaikallinen(oppiaine) ? (
          <span className="koodi-ja-nimi">
            <span className="koodi">
              <Editor
                model={oppiaine}
                path="tunniste.koodiarvo"
                placeholder={t('Koodi')}
              />
            </span>
            <span className="nimi">
              <Editor
                model={fixKuvaus(oppiaine)}
                path="tunniste.nimi"
                placeholder={t('Oppiaineen nimi')}
              />
            </span>
          </span>
        ) : showExpand ? (
          <button
            className="nimi inline-text-button"
            onClick={() => onExpand(!expanded)}
          >
            {oppiaineTitle(oppiaine)}
          </button>
        ) : (
          <span className="nimi">{oppiaineTitle(oppiaine)}</span>
        )}
        {
          // kielivalinta
          isKieliaine(oppiaine) && (
            <span className="value kieli">
              <Editor
                model={oppiaine}
                inline={true}
                path="kieli"
                sortBy={sortLanguages}
              />
            </span>
          )
        }
        {this.state &&
          this.state.changed &&
          isPaikallinen(oppiaine) &&
          doActionWhileMounted(oppiaine.context.saveChangesBus, () => {
            const data = modelData(oppiaine)
            const organisaatioOid = modelData(oppiaine.context.toimipiste).oid
            const key = data.tunniste.koodiarvo
            saveOrganizationalPreference(
              organisaatioOid,
              paikallinenOppiainePrototype(uusiOppiaineenSuoritus).value
                .classes[0],
              key,
              data
            )
          })}
      </span>
    )
  }

  UNSAFE_componentWillReceiveProps(newProps) {
    const currentData = modelData(this.props.oppiaine)
    const newData = modelData(newProps.oppiaine)
    if (!equals(currentData, newData)) {
      this.setState({ changed: true })
    }
  }
}

const fixKuvaus = (oppiaine) => {
  return lensedModel(
    oppiaine,
    L.rewrite((m) => {
      const nimi = modelLookup(m, 'tunniste.nimi').value
      return hasModelProperty(m, 'kuvaus')
        ? modelSetValue(m, nimi, 'kuvaus')
        : m
    })
  )
}

export const paikallinenOppiainePrototype = (oppiaineenSuoritus) => {
  return koulutusModuuliprototypes(oppiaineenSuoritus).find(isPaikallinen)
}

const oppiaineTitle = (aine) => {
  const kieliaine = isKieliaine(aine)
  const title = t(modelData(aine, 'tunniste.nimi')) + (kieliaine ? ', ' : '')
  return title
}
