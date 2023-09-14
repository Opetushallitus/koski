import React from 'react'
import { addContext, modelEmpty, modelItems } from './EditorModel.ts'
import { Editor } from './Editor'
import { ArrayEditor } from './ArrayEditor'
import {
  checkOnlyWhen,
  modelErrorMessages,
  modelProperties
} from './EditorModel'
import Text from '../i18n/Text'
import { buildClassNames } from '../components/classnames'
import { flatMapArray } from '../util/util'
import { KoulusivistyskieliPropertyTitle } from '../suoritus/Koulusivistyskieli'
import { PuhviKoePropertyTitle } from '../suoritus/PuhviKoePropertyTitle'
import { SuullisenKielitaidonKoePropertyTitle } from '../suoritus/SuullisenKielitaidonKoePropertyTitle'
import { PropertyInfo } from './PropertyInfo'
import classNames from 'classnames'
import { onEshPäätasonKoulutusmoduuliProperty } from '../esh/esh'
import { EshPäätasonKoulutusmoduuliPropertyTitle } from '../esh/EshPaatasonKoulutusmoduuliPropertyTitle'

export class PropertiesEditor extends React.Component {
  render() {
    const defaultValueEditor = (prop, getDefault) => getDefault()
    let {
      properties,
      model,
      context,
      getValueEditor = defaultValueEditor,
      propertyFilter = () => true,
      propertyEditable = (p) => p.editable || context.editAll,
      className,
      showAnyway = () => false
    } = this.props
    if (!properties) {
      if (!model) throw new Error('model or properties required')
      properties = modelProperties(model)
    }
    if (!context) {
      if (!model) throw new Error('model or context required')
      context = model.context
    }
    const edit = context.edit
    const contextForProperty = (property) =>
      !propertyEditable(property) && context.edit
        ? { ...context, edit: false }
        : context
    const shouldShow = (property) =>
      (shouldShowProperty(contextForProperty(property))(property) &&
        propertyFilter(property)) ||
      showAnyway(property)

    const munch = (prefix) => (property, i) => {
      if (
        property.flatten &&
        property.model.value &&
        property.model.value.properties
      ) {
        return flatMapArray(
          modelProperties(property.model, shouldShow),
          munch(prefix + property.key + '.')
        )
      } else if (!edit && property.flatten && property.model.type === 'array') {
        return flatMapArray(modelItems(property.model), (item, j) => {
          return flatMapArray(
            modelProperties(item, shouldShow),
            munch(prefix + j + '.')
          )
        })
      } else {
        const key = prefix + property.key + i
        const propertyClassName = classNames('property', property.key)
        const valueClass = classNames('value', {
          empty: modelEmpty(property.model)
        })
        const valueEditor = (
          <ErrorDecorator
            model={property.model}
            parentKey={key}
            editor={
              property.tabular ? (
                <TabularArrayEditor model={property.model} />
              ) : (
                getValueEditor(property, () => (
                  <Editor
                    model={
                      propertyEditable(property)
                        ? property.model
                        : addContext(property.model, { edit: false })
                    }
                  />
                ))
              )
            }
          />
        )

        return [
          <tr className={propertyClassName} key={key}>
            {property.complexObject ? (
              <td className="complex" colSpan="2">
                <div className="label">
                  <PropertyTitle property={property} />
                  <PropertyInfo property={property} />
                </div>
                <div
                  className={valueClass}
                  data-testid={`${property.key}-complex-value`}
                >
                  {valueEditor}
                </div>
              </td>
            ) : (
              [
                <td
                  className="label"
                  key="label"
                  data-testid={`${property.key}-label`}
                >
                  <PropertyTitle property={property} />
                  <PropertyInfo property={property} />
                </td>,
                <td
                  className={valueClass}
                  key="value"
                  data-testid={`${property.key}-value`}
                >
                  {valueEditor}
                </td>
              ]
            )}
          </tr>
        ]
      }
    }

    const tableContents = flatMapArray(properties.filter(shouldShow), munch(''))
    return tableContents && tableContents.length > 0 ? (
      <div className={buildClassNames(['properties', className])}>
        <table>
          <tbody>{tableContents}</tbody>
        </table>
      </div>
    ) : null
  }
}
PropertiesEditor.canShowInline = () => false

const ErrorDecorator = ({ editor, model, parentKey }) => {
  const errorMsgs =
    (model.context &&
      modelErrorMessages(model).map((error, i) => (
        <div key={parentKey + '-' + i} className="propertyError error">
          {error}
        </div>
      ))) ||
    []
  return [
    <React.Fragment key={'editor-' + parentKey}>{editor}</React.Fragment>
  ].concat(errorMsgs)
}

export const PropertyTitle = ({ property }) => {
  const description = property.description && property.description.join(' ')
  if (property.title === 'Koulusivistyskieli') {
    return <KoulusivistyskieliPropertyTitle />
  } else if (property.title === 'Suullisen kielitaidon kokeet') {
    return <SuullisenKielitaidonKoePropertyTitle />
  } else if (property.title === 'Puhvi-koe') {
    return <PuhviKoePropertyTitle />
  } else if (onEshPäätasonKoulutusmoduuliProperty(property)) {
    return <EshPäätasonKoulutusmoduuliPropertyTitle title={property.title} />
  } else if (description) {
    return (
      <Text
        name={property.title}
        title={description}
        className="with-description"
      />
    )
  } else {
    return <Text name={property.title} />
  }
}

export const shouldShowProperty = (context) => (property) => {
  if (!context.edit && modelEmpty(property.model)) return false
  if (property.hidden) return false
  if (property.deprecated && modelEmpty(property.model)) return false
  if (property.onlyWhen && !checkOnlyWhen(property.owner, property.onlyWhen))
    return false
  return true
}

export class TabularArrayEditor extends React.Component {
  render() {
    const { model } = this.props
    const items = modelItems(model)
    if (!items.length) return null
    if (model.context.edit) return <ArrayEditor {...this.props} />
    const properties = modelProperties(items[0]).filter(notHidden)
    return (
      <table className="tabular-array">
        <thead>
          <tr>
            {properties.map((p, i) => (
              <th key={i}>
                <PropertyTitle property={p} />
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {items.map((item, i) => (
            <tr key={i}>
              {modelProperties(item)
                .filter(notHidden)
                .map((p, j) => {
                  return (
                    <td key={j}>
                      <Editor model={p.model} />
                    </td>
                  )
                })}
            </tr>
          ))}
        </tbody>
      </table>
    )
  }
}

const notHidden = (p) => !p.hidden
