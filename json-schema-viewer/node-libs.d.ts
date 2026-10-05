declare module '@babel/helper-compilation-targets' {
  const helperCompilationTargets: {
    default: (
      inputTargets?: Record<string, unknown>,
      options?: { configPath?: string }
    ) => Record<string, string>
  }
  export = helperCompilationTargets
}
