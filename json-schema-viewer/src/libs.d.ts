declare module 'highlight.js/lib/highlight' {
  const hljs: {
    registerLanguage: (name: string, language: unknown) => void
    highlightBlock: (block: Element) => void
  }
  export = hljs
}

declare module 'highlight.js/lib/languages/*' {
  const language: unknown
  export = language
}
