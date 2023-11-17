import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import { CommonProps } from '../components-v2/CommonProps'
import { RaisedButton } from '../components-v2/controls/RaisedButton'
import { t } from '../i18n/i18n'
import { assertNever } from '../util/selfcare'
import { useTestId } from './useTestId'

export type TreeContext = {
  id: string
  action?: TreeAction
}

export type TreeAction = OpenAllAction | CloseAllAction

export type OpenAllAction = {
  type: 'openAll'
  origin: string
}

export type CloseAllAction = {
  type: 'closeAll'
  origin: string
}

const rootContext: TreeContext = {
  id: ''
}

const TreeContext = React.createContext(rootContext)

export type TreeHook = {
  TreeNode: TreeNodeComponent
  isOpen: boolean
  toggle: () => void
  toggleAll: () => void
}

export type TreeNodeComponent = React.FC<{ children: React.ReactNode }>

const random = (): string => Math.random().toString().slice(2)

export const useTree = (initiallyOpen = false): TreeHook => {
  // Tunnisteet
  const parent = useContext(TreeContext)
  const id = useMemo(() => `${parent.id}/${random()}`, [parent.id])

  // Oma tila
  const [isOpen, setOpen] = useState(initiallyOpen)
  const toggle = useCallback(() => setOpen(!isOpen), [isOpen])

  // Vanhempien lähettämien viestien käsittely
  const [action, setAction] = useState<TreeAction | null>(null)
  useEffect(() => {
    setAction(null) // Uusi action vanhemmalta jyrää yli edellisen toiminnon
    if (parent.action && id.startsWith(parent.action.origin)) {
      const type = parent.action.type
      switch (type) {
        case 'openAll':
          setOpen(true)
          break
        case 'closeAll':
          setOpen(false)
          break
        default:
          assertNever(type)
      }
    }
  }, [id, parent.action])

  // Lapsille lähetettävien viestien dispatchaus
  const toggleAll = useCallback(() => {
    setOpen(!isOpen)
    setAction({ type: isOpen ? 'closeAll' : 'openAll', origin: id })
  }, [id, isOpen])

  // Lapsioksille jaettava konteksti
  const contextValue = useMemo(
    () => ({ id, action: action || parent.action }),
    [action, id, parent.action]
  )

  const TreeNode: TreeNodeComponent = useMemo(
    () => (props) =>
      (
        <TreeContext.Provider value={contextValue}>
          {props.children}
        </TreeContext.Provider>
      ),
    [contextValue]
  )

  return {
    TreeNode,
    isOpen,
    toggle,
    toggleAll
  }
}

export type OpenAllButtonProps = CommonProps<
  Pick<TreeHook, 'isOpen' | 'toggleAll'>
>

export const OpenAllButton: React.FC<OpenAllButtonProps> = (props) => {
  return (
    <RaisedButton
      data-testid={useTestId('expandAll')}
      onClick={(e) => {
        e.preventDefault()
        props.toggleAll()
      }}
    >
      {props.isOpen ? t('Sulje kaikki') : t('Avaa kaikki')}
    </RaisedButton>
  )
}
