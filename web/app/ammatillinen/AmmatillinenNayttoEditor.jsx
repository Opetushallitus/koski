import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import Text from '../i18n/Text'
import ModalDialog from '../editor/ModalDialog'
import {wrapOptional} from '../editor/EditorModel'
import {accumulateModelStateAndValidity, pushModel, resetOptionalModel} from '../editor/EditorModel'
import {PropertiesEditor} from '../editor/PropertiesEditor'
import {addContext, modelLookup} from '../editor/EditorModel'
import {Editor} from '../editor/Editor'
import {sortGrades} from '../util/sorting'

const NäyttöPopup = ({model, hasOldData, doneCallback}) => {
  const {modelP, errorP} = accumulateModelStateAndValidity(model)
  const validP = errorP.not()
  const submitB = Bacon.Bus()

  submitB.map(modelP).onValue(m => {
    pushModel(m, model.context.changeBus)
    doneCallback()
  })

  return (
    <ModalDialog className="lisää-näyttö-modal" onDismiss={doneCallback} onSubmit={() => submitB.push()} submitOnEnterKey="false" okTextKey={hasOldData ? 'Päivitä' : 'Lisää'} validP={validP}>
      <h2><Text name="Ammattiosaamisen näyttö"/></h2>

      <PropertiesEditor
        baret-lift
        model={modelP}
        propertyFilter={p => !['arviointikohteet', 'haluaaTodistuksen', 'hylkäyksenPeruste', 'suoritusaika'].includes(p.key)}
        getValueEditor={(p, getDefault) => {
          if (p.key === 'suorituspaikka') {return (
            <table><tbody><tr>
              {
                modelP.map(m => [
                  <td><Editor model={modelLookup(m, 'suorituspaikka.tunniste')}/></td>,
                  <td><Editor model={modelLookup(m, 'suorituspaikka.kuvaus')}/></td>
                ])
              }
            </tr></tbody></table>
          )}
          if (p.key === 'arvosana') {
            return <Editor model={p.model} sortBy={sortGrades}/>
          }
          return getDefault()
        }}
      />
    </ModalDialog>
  )
}

const YksittäinenNäyttöEditor = ({edit, model, popupVisibleA}) => {
  return (<div>
    {edit && <a className="edit-value" onClick={() => popupVisibleA.set(true)}><Text name="Muokkaa"/></a>}
    {edit && <a className="remove-value" onClick={() => resetOptionalModel(model)}></a>}

    <div className="näyttö-rivi">
      <PropertiesEditor model={ addContext(model, { edit: false })}/>
    </div>
  </div>)
}

export class AmmatillinenNäyttöEditor extends React.Component {
  constructor(props) {
    super(props)
    this.state = {
      popupVisibleA: Atom(false)
    }
  }

  render() {
    const model = this.props.model
    const popupVisibleA = this.state.popupVisibleA
    const edit = model.context.edit

    const wrappedModel = wrapOptional(model)
    const hasData = model.modelId !== 0

    return (
      <div>
        {popupVisibleA.map(visible => visible
          ? <NäyttöPopup edit={edit} hasOldData={hasData} model={wrappedModel} doneCallback={() => popupVisibleA.set(false)}/>
          : null)
        }
        {hasData &&
          <YksittäinenNäyttöEditor edit={edit} model={model} popupVisibleA={popupVisibleA}/>
        }
        {edit && !hasData &&
          <a className="add-value" onClick={() => popupVisibleA.set(true)}><Text name="Lisää ammattiosaamisen näyttö"/></a>
        }
      </div>
    )
  }
}

AmmatillinenNäyttöEditor.handlesOptional = () => true
AmmatillinenNäyttöEditor.writeOnly = true
