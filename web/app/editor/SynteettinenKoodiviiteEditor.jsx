import React, { useCallback } from 'react'
import { modelData } from './EditorModel.ts'
import {
  wrapOptional,
  pushModelValue,
  modelValid,
  findModelProperty
} from './EditorModel'
import classNames from 'classnames'
import {
  isEshFinalArvosana,
  isEshNumericalArvosana,
  isEshPreliminaryArvosana,
  isEshSynteettinenKoodisto,
  isValidEshSynteettinenKoodiarvo
} from '../esh/europeanschoolofhelsinkiSuoritus'
import Text from '../i18n/Text'

export const SynteettinenKoodiviiteEditor = ({ model, autoFocus }) => {
  const wrappedModel = wrapOptional(model)
  const data = modelData(model)

  const koodiarvoProperty = findModelProperty(
    wrappedModel,
    (p) => p.key === 'koodiarvo'
  )

  const onChange = useCallback(
    (event) => {
      pushModelValue(koodiarvoProperty.model, { data: event.target.value })
    },
    [koodiarvoProperty.model]
  )

  const error = !modelValid(model)
  const className = classNames('editor-input', {
    error:
      error ||
      (isEshSynteettinenKoodisto(data) &&
        !isValidEshSynteettinenKoodiarvo(data)),
    valid:
      !error ||
      (isEshSynteettinenKoodisto(data) && isValidEshSynteettinenKoodiarvo(data))
  })

  return model.context.edit ? (
    <input
      className={className}
      type="text"
      defaultValue={data && data.koodiarvo ? data.koodiarvo : ''}
      onChange={onChange}
      autoFocus={autoFocus}
    />
  ) : (
    <span className="inline string">
      {data && data.koodiarvo ? data.koodiarvo : ''}
    </span>
  )
}

SynteettinenKoodiviiteEditor.handlesOptional = () => false
SynteettinenKoodiviiteEditor.isEmpty = (m) => !modelData(m)
SynteettinenKoodiviiteEditor.canShowInline = () => true
SynteettinenKoodiviiteEditor.validateModel = (model) => {
  const data = modelData(model)
  // TOR-1685 ESH-validaatiot
  if (
    isEshSynteettinenKoodisto(data) &&
    !isValidEshSynteettinenKoodiarvo(data)
  ) {
    if (isEshFinalArvosana(data)) {
      return [
        {
          key: 'invalid_esh_final_grade',
          message: (
            <>
              <Text name="Arvosanan tulee olla 10, tai luku väliltä 0.00 - 9.99 tasan kahden desimaalin tarkkuudella." />
              <br />
              <Text name="Esimerkkejä: 9.58 tai 8.19" />
            </>
          )
        }
      ]
    } else if (isEshNumericalArvosana(data)) {
      return [
        {
          key: 'invalid_esh_numerical_grade',
          message: (
            <>
              <Text name="Arvosanan tulee olla 0, 10, tai luku siltä väliltä yhden desimaalin tarkkuudella." />
              <br />
              <Text name="Esimerkkejä: 9.5 tai 8.0" />
            </>
          )
        }
      ]
    } else if (isEshPreliminaryArvosana(data)) {
      return [
        {
          key: 'invalid_esh_preliminary_grade',
          message: (
            <>
              <Text name="Arvosanan tulee olla 0, 10 tai luku siltä väliltä yhden desimaalin tarkkuudella." />
              <br />
              <Text name="Esimerkkejä: 9.2 tai 8.6" />
            </>
          )
        }
      ]
    }
  }
  if (!model.optional && !data) {
    return [{ key: 'missing' }]
  }
}
