import { Context, useContext, useEffect, useState } from 'react'

export const useDebugChanges = (a: any) => {
  const json = JSON.stringify(a, null, 2)
  const [prev, setPrev] = useState(json)
  useEffect(() => {
    if (json !== prev) {
      console.log('Changed:', prev, '---->', json)
      setPrev(json)
    }
  }, [json, prev])
}

export const useLogPropUpdates = <T extends object>(
  props: T,
  container: string
) => {
  Object.entries(props).forEach(([name, prop]) => {
    // eslint-disable-next-line react-hooks/rules-of-hooks
    useEffect(() => {
      console.log(`${container}.${name} updated:`, prop)
    }, [name, prop])
  })
}

export const debugReducer =
  <S, A>(reducer: (state: S, action: A) => S) =>
  (state: S, action: A): S => {
    const before = JSON.parse(JSON.stringify(state))
    const after = reducer(state, action)
    // @ts-ignore
    //console.log('Reducer:', action?.type, { before, after, action })
    if (action.type === 'modify') {
      console.log(
        'Reducer',
        // @ts-ignore
        after?.data?.suoritukset?.[0]?.osasuoritukset[3]?.koulutusmoduuli
          ?.laajuus?.arvo
      )
    }
    return after
  }
