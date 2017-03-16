import React from 'react'
import R from 'ramda'
import {contextualizeModel} from './EditorModel.js'
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

    model.context.errorBus
      .filter(e => e[0].path.startsWith(model.context.path))
      .scan({}, (p, e) => Object.assign(p, R.objOf(e[0].path, e[1].error)))
      .map(e => R.reduce((acc, error) => acc || error[1], false, R.toPairs(e)))
      .takeUntil(this.unmountE)
      .onValue(hasErrors => this.setState({ hasErrors }))
  },
  getInitialState() {
    return {
      hasErrors: false
    }
  }
})
