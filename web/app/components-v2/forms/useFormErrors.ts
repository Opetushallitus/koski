import { useMemo } from 'react'
import { parsePath } from '../../util/optics'
import { FormModel, FormOptic } from './FormModel'
import { errorPathIs } from './validator'

export const useFormErrors = <S extends object, A extends object>(
  form: FormModel<S>,
  path: FormOptic<S, A> | string
) => {
  const pathStr = useMemo(() => {
    if (form.editMode) {
      return typeof path === 'string' ? path : parsePath(path, form.state)
    }
    return undefined
  }, [path, form.editMode])

  return useMemo(
    () =>
      pathStr
        ? form.errors.filter(errorPathIs((path) => path.startsWith(pathStr)))
        : [],
    [pathStr, form.errors]
  )
}
