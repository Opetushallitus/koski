export type Oid = `1.${number}.${number}.${number}.${number}.${number}.${number}`
export type ISODate = `${number}-${number}-${number}`

export type Language = "fi" | "sv" | "en"
export type LocalizedString = Partial<Record<Language, string>>
