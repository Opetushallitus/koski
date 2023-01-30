import * as E from 'fp-ts/Either'
import * as C from '../util/constraints'
import { pipe } from 'fp-ts/lib/function'
import React, {
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState
} from 'react'
import { Constraint } from '../types/fi/oph/koski/typemodel/Constraint'
import { fetchConstraint } from '../util/koskiApi'
import { ClassOf, ObjWithClass, schemaClassName } from '../util/types'

const Loading = Symbol('loading')

export type ConstraintsRecord = Record<string, Constraint | typeof Loading>

export type ConstraintsContextValue = {
  readonly constraints: ConstraintsRecord
  readonly loadConstraint: (schemaClass: string) => void
}

const ConstraintsContext = React.createContext<ConstraintsContextValue>({
  constraints: {},
  loadConstraint: () => {}
})

export type ConstraintsProviderProps = {
  children: React.ReactNode
}

class ConstraintsLoader {
  constraints: ConstraintsRecord = {}

  async loadConstraint(schemaClass: string): Promise<boolean> {
    if (!this.constraints[schemaClass]) {
      this.constraints[schemaClass] = Loading

      pipe(
        await fetchConstraint(schemaClass),
        E.map((response) => {
          this.constraints = {
            ...this.constraints,
            [schemaClass]: response.data
          }
        })
      )
      return true
    }
    return false
  }
}

const constraintsLoaderSingleton = new ConstraintsLoader()

export const ConstraintsProvider = (props: ConstraintsProviderProps) => {
  const [constraints, setConstraints] = useState<ConstraintsRecord>({})

  const loadConstraint = useCallback(async (schemaClass: string) => {
    if (await constraintsLoaderSingleton.loadConstraint(schemaClass)) {
      setConstraints(constraintsLoaderSingleton.constraints)
    }
  }, [])

  const providedValue: ConstraintsContextValue = useMemo(
    () => ({ constraints, loadConstraint }),
    [constraints, loadConstraint]
  )

  return (
    <ConstraintsContext.Provider value={providedValue}>
      {props.children}
    </ConstraintsContext.Provider>
  )
}

/**
 * Antaa skeemaluokan nimeä vastaavan constraintin.
 * @param className skeemaluokan nimi pitkässä tai lyhyessä muodossa (esim. "fi.oph.koski.schema.Vahvistus" tai pelkkä "Vahvistus")
 * @returns Rakennekuvauksen sekä validointitietoja
 */
export const useSchema = (className?: string | null): Constraint | null => {
  const { constraints, loadConstraint } = useContext(ConstraintsContext)
  const schemaClass = useMemo(
    () => (className && schemaClassName(className)) || className,
    [className]
  )
  useEffect(() => {
    if (schemaClass) {
      loadConstraint(schemaClass)
    }
  }, [loadConstraint, schemaClass])
  const constraint = schemaClass && constraints[schemaClass]
  return typeof constraint === 'object' ? constraint : null
}

export const useAllowedStrings = (
  className: string | null | undefined,
  path: string
): string[] | null => {
  const c = useSchema(className)
  return useMemo(() => pipe(c, C.path(path), C.allowedStrings), [c, path])
}

export const useChildClassName = <T extends ObjWithClass>(
  className: string | null | undefined,
  path: string
): ClassOf<T> | null => {
  const c = useSchema(className)
  return useMemo(() => pipe(c, C.path(path), C.className<T>()), [c, path])
}
