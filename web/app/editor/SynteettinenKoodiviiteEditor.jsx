import React from 'react'
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

const isSynteettinenKoodisto = (koodistoUri) => {
  return isEshSynteettinenKoodisto(koodistoUri)
}

const isValidSynteettinenKoodiarvo = (koodistoUri, koodiarvo) => {
  return isValidEshSynteettinenKoodiarvo(koodistoUri, koodiarvo)
}

export const SynteettinenKoodiviiteEditor = ({ model, autoFocus }) => {
  const wrappedModel = wrapOptional(model)

  console.log(wrappedModel)

  const koodiarvoProperty = findModelProperty(
    wrappedModel,
    (p) => p.key === 'koodiarvo'
  )
  const koodistoUriProperty = findModelProperty(
    wrappedModel,
    (p) => p.key === 'koodistoUri'
  )
  const koodiarvo = modelData(koodiarvoProperty.model)
  const koodistoUri = modelData(koodistoUriProperty.model)

  const onChange = (event) => {
    pushModelValue(koodiarvoProperty.model, { data: event.target.value })
  }

  const error = !modelValid(model)
  const className = classNames('editor-input', 'synthetic', {
    error:
      error ||
      (isSynteettinenKoodisto(koodistoUri) &&
        !isValidSynteettinenKoodiarvo(koodistoUri, koodiarvo)),
    valid:
      !error ||
      (isSynteettinenKoodisto(koodistoUri) &&
        isValidSynteettinenKoodiarvo(koodistoUri, koodiarvo))
  })

  return model.context.edit ? (
    <input
      className={className}
      type="text"
      defaultValue={koodiarvo}
      onChange={onChange}
      autoFocus={autoFocus}
    />
  ) : (
    <span className={classNames('inline', 'string', 'synthetic')}>
      {koodiarvo}
    </span>
  )
}

SynteettinenKoodiviiteEditor.handlesOptional = () => false
SynteettinenKoodiviiteEditor.isEmpty = (m) => {
  return !modelData(m)
}
SynteettinenKoodiviiteEditor.canShowInline = () => true
SynteettinenKoodiviiteEditor.validateModel = (model) => {
  const koodiarvo = modelData(model, 'koodiarvo')
  const koodistoUri = modelData(model, 'koodistoUri')

  if (
    isEshSynteettinenKoodisto(koodistoUri) &&
    !isValidEshSynteettinenKoodiarvo(koodistoUri, koodiarvo)
  ) {
    if (isEshFinalArvosana(koodistoUri)) {
      return [
        {
          key: 'invalid_esh_final_grade',
          message: (
            <>
              <Text name="description:invalid_esh_final_grade" />
              <br />
              <Text name="description:invalid_esh_final_grade_example" />
            </>
          )
        }
      ]
    } else if (isEshNumericalArvosana(koodistoUri)) {
      return [
        {
          key: 'invalid_esh_numerical_grade',
          message: (
            <>
              <Text name="description:invalid_esh_numerical_grade" />
              <br />
              <Text name="description:invalid_esh_numerical_grade_example" />
            </>
          )
        }
      ]
    } else if (isEshPreliminaryArvosana(koodistoUri)) {
      return [
        {
          key: 'invalid_esh_preliminary_grade',
          message: (
            <>
              <Text name="description:invalid_esh_preliminary_grade" />
              <br />
              <Text name="description:invalid_esh_preliminary_grade_example" />
            </>
          )
        }
      ]
    }
  }
  if (!model.optional && koodiarvo === '') {
    return [{ key: 'missing' }]
  }
}
