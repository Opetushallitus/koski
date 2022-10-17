import React, { useCallback, useRef } from "react"

export type WithScrollProps = {
  scrollTop: () => void
}

export const withScroll =
  <P extends object>(
    Component: React.ComponentType<P & WithScrollProps>
  ): React.FC<P> =>
  (props: P) => {
    const head = useRef<HTMLDivElement>(null)
    const scrollTop = useCallback(() => {
      if (head.current) {
        safeScrollIntoView(head.current)
      }
    }, [])

    return (
      <>
        <div ref={head} />
        <Component {...props} scrollTop={scrollTop} />
      </>
    )
  }

const safeScrollIntoView = (element: Element) => {
  // scrollIntoView crashes Jest
  if (process.env.NODE_ENV !== "test") {
    element.scrollIntoView({ behavior: "smooth" })
  }
}
