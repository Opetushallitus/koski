import { useEffect, useMemo, useRef, useState } from "react"

export type UseBoundClientRectValue = {
  ref: React.RefObject<HTMLElement>
  rect: DOMRect | null
}

export const useBoundingClientRect = (): UseBoundClientRectValue => {
  const ref = useRef<HTMLElement>(null)
  const [rect, setRect] = useState<DOMRect | null>(null)

  useEffect(() => {
    const update = () => setRect(ref.current?.getBoundingClientRect() || null)
    update()
    window.addEventListener("resize", update)
    return () => window.removeEventListener("resize", update)
  }, [])

  const result = useMemo(() => ({ ref, rect }), [rect])

  return result
}
