export const joinClassNames = (...names: Array<string | undefined>) =>
  names.filter((n) => n).join(" ")
