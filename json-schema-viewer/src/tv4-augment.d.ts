import type { SchemaMap } from 'tv4'

declare module 'tv4' {
  interface TV4 {
    asyncLoad: (
      uri: string[] | false | undefined,
      callback?: (schemaMap: SchemaMap) => void,
      uriPrefix?: string
    ) => true | undefined
  }
}
