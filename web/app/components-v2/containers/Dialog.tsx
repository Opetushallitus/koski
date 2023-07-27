import React, { useCallback, useMemo, useRef } from 'react'
import { t } from '../../i18n/i18n'
import { RaisedButton } from '../controls/RaisedButton'

type DialogProps = React.DetailedHTMLProps<
  React.DialogHTMLAttributes<HTMLDialogElement>,
  HTMLDialogElement
>
/**
 * useDialog-hookilla luodaan dialog-tyyppisiä elementtejä ohjelmallisesti.
 *
 * Esimerkki:
 *
 * ```tsx
 * import { useRef } from 'react'
 *
 * const MyDialog = () => {
 *   const { closeDialog, openDialog, Dialog } = useDialog('MyDialog')
 *   return (
 *     <>
 *       <button onClick={(_e) => openDialog()}>{'Avaa'}</button>
 *       <Dialog>{'Hello world'}</Dialog>
 *     </>
 *   )
 * }
 * ```
 */
export const useDialog = (id: string) => {
  const dialogRef = useRef<HTMLDialogElement | null>(null)
  const closeDialog = useCallback(() => {
    if (dialogRef.current !== null) {
      dialogRef.current.close()
    }
  }, [dialogRef])
  const openDialog = useCallback(() => {
    if (dialogRef.current !== null) {
      dialogRef.current.showModal()
    }
  }, [dialogRef])

  const DialogComponent = useMemo(() => {
    const Dialog: React.FC<React.PropsWithChildren<DialogProps>> = (props) => {
      const { ref: _ref, children, ...restProps } = props
      return (
        <dialog ref={dialogRef} className="Dialog-Container" {...restProps}>
          <div className="Dialog-Container__Children">
            {children}
            <div>
              <RaisedButton
                onClick={(e) => {
                  e.preventDefault()
                  closeDialog()
                }}
              >
                {t('Sulje')}
              </RaisedButton>
            </div>
          </div>
        </dialog>
      )
    }
    Dialog.displayName = `${id}-Dialog`
    return Dialog
  }, [closeDialog, id])

  return { closeDialog, openDialog, ref: dialogRef, Dialog: DialogComponent }
}
