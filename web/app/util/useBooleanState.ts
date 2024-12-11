import { useCallback, useMemo, useState } from "react";

export function useBooleanState(initialState: boolean): [boolean, () => void, () => void] {
    const [state, setState] = useState(initialState);
    const setTrue = useCallback(() => setState(true), [])
    const setFalse = useCallback(() => setState(false), [])
    return useMemo(() => [state, setTrue, setFalse], [setFalse, setTrue, state])
}