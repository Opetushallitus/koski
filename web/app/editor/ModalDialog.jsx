import React from 'baret'
import Bacon from 'baconjs'
import Atom from 'bacon.atom'
import { doActionWhileMounted, parseBool } from '../util/util'
import Text from '../i18n/Text'
import { t } from '../i18n/i18n'
import FocusLock from 'react-focus-lock'

export default ({
  className,
  onDismiss,
  onSubmit,
  children,
  submitOnEnterKey,
  okTextKey = 'Ok',
  cancelTextKey = 'Peruuta',
  validP = Bacon.constant(true),
  fullscreen = false
}) => {
  submitOnEnterKey = parseBool(submitOnEnterKey, true)
  const submittedAtom = Atom(false)
  const keyE = Bacon.fromEvent(document, 'keyup')
    .combine(validP, (e, valid) => ({ e, valid }))
    .filter(({ e, valid }) => valid || e.keyCode !== 13) // skip enter key events while not valid
    .map('.e')

  function handleKeys(e) {
    if (e.keyCode == 27) onDismiss()
    if (e.keyCode == 13 && submitOnEnterKey) onSubmit()
  }
  const classNameP = submittedAtom.map(
    (submitted) =>
      (className || '') +
      ' modal' +
      (submitted ? ' submitted' : '') +
      (fullscreen ? ' fullscreen textstyle-body' : '')
  )

  const Modal = () => (
    <div
      className={classNameP}
      role="dialog"
      aria-modal={true}
      aria-describedby="modal-main-content"
    >
      <div className="modal-shield" onClick={() => onDismiss()} />
      <div className="modal-content">
        <button
          className="close-modal"
          onClick={() => onDismiss()}
          aria-label={t('Sulje')}
        />
        <div id="modal-main-content">{children}</div>
        <div className="actions">
          <button
            className="peruuta koski-button koski-button-secondary"
            onClick={() => onDismiss()}
          >
            <Text name={cancelTextKey} />
          </button>
          <button
            className="vahvista koski-button"
            disabled={validP.not().or(submittedAtom)}
            onClick={(e) => {
              e.preventDefault()
              submittedAtom.set(true)
              onSubmit()
            }}
            data-testid="dialog-vahvista"
          >
            <Text name={okTextKey} />
          </button>
        </div>
      </div>
      {doActionWhileMounted(keyE, handleKeys)}
    </div>
  )

  return fullscreen ? (
    <FocusLock autoFocus={true} returnFocus={true}>
      <Modal />
    </FocusLock>
  ) : (
    <Modal />
  )
}
