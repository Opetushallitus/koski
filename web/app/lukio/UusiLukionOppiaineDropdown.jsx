import React from 'baret'
import {t} from '../i18n/i18n'
import {is} from '../util/util'
import {UusiOppiaineDropdown} from '../oppiaine/UusiOppiaineDropdown'
import {ensureArrayKey, modelData, modelItems, modelSet, modelSetTitle, pushModel} from '../editor/EditorModel'
import {newOsasuoritusProto} from '../suoritus/Suoritus'
import * as R from 'ramda'
import {koulutusModuuliprototypes} from '../suoritus/Koulutusmoduuli'

export const UusiLukionOppiaineDropdown = ({model, oppiaineenSuoritusClasses}) => {
  if (!model || !model.context.edit) return null

  const addOppiaine = oppiaine => {
    const nimi = t(modelData(oppiaine, 'tunniste.nimi'))
    const oppiaineWithTitle = modelSetTitle(oppiaine, nimi)
    const suoritusUudellaOppiaineella = modelSet(
      oppiaine.parent || newOsasuoritusProto(model, oppiaineenSuoritusClasses ? oppiaineenSuoritusClasses[0] : undefined),
      oppiaineWithTitle,
      'koulutusmoduuli'
    )
    pushModel(suoritusUudellaOppiaineella, model.context.changeBus)
    ensureArrayKey(suoritusUudellaOppiaineella)
  }

  const suoritusProtos = oppiaineenSuoritusClasses
    ? oppiaineenSuoritusClasses.map(c => newOsasuoritusProto(model, c))
    : [newOsasuoritusProto(model)]

  const oppiainePrototypes = R.flatten(suoritusProtos.map(koulutusModuuliprototypes)).filter(laajuudetonLukionOppiaine)

  return (
    <UusiOppiaineDropdown
      suoritukset={modelItems(model, 'osasuoritukset')}
      oppiaineenSuoritukset={suoritusProtos}
      organisaatioOid={modelData(model, 'toimipiste.oid')}
      resultCallback={addOppiaine}
      placeholder={t('Lisää oppiaine')}
      pakollinen={true}
      oppiainePrototypes={oppiainePrototypes}
    />
  )
}

// Lukion oppiaineet toteuttaa kaksi versiota koulutusmoduuleja, karsitaan laajuudelliset pois
const laajuudetonLukionOppiaine = proto => {
  return !is(proto).instanceOf('lukionoppiaine') || is(proto).instanceOf('laajuudeton')
}
