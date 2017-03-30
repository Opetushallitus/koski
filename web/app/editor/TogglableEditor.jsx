import React from 'react'
import R from 'ramda'
import {contextualizeModel, accumulateErrors} from './EditorModel.js'
import BaconComponent from '../BaconComponent'

export const TogglableEditor = BaconComponent({
  render() {
    let { model, renderChild } = this.props
    let context = model.context
    let { hasErrors } = this.state
    let edit = context.edit || (this.state && this.state.edit)
    let toggleEdit = () => {
      if (edit) {
        context.doneEditingBus.push()
      }
      this.setState({edit: !edit})
    }
    let showToggleEdit = model.editable && !context.edit && !context.hasToggleEdit
    let modifiedContext = R.merge(context, {
      edit: edit,
      hasToggleEdit: context.hasToggleEdit || showToggleEdit  // to prevent nested duplicate "edit" links
    })
    let editLink = showToggleEdit
      ? hasErrors && edit
        ? <span className="toggle-edit disabled">valmis</span>
        : <a className={edit ? 'toggle-edit editing' : 'toggle-edit'} onClick={toggleEdit}>{edit ? 'valmis' : 'muokkaa'}</a>
      : null

    return (renderChild(contextualizeModel(model, modifiedContext), editLink))
  },
  componentDidMount() {
    let {model} = this.props

    accumulateErrors(model.context.errorBus, model.context.path)
      .takeUntil(this.unmountE)
      .onValue(hasErrors => this.setState({ hasErrors }))
  },
  getInitialState() {
    return {
      hasErrors: false
    }
  }
})
