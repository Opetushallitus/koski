export const runningLocally: boolean = ["local", "unittest"].includes(
  window.environment || ""
)
