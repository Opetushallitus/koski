export const intersects = <T>(as: T[], bs: T[]): boolean =>
  as.find((a) => bs.includes(a)) !== undefined
