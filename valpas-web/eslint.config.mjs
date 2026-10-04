import tseslint from "typescript-eslint"
import eslintConfigPrettier from "eslint-config-prettier"
import compat from "eslint-plugin-compat"
import reactHooks from "eslint-plugin-react-hooks"

export default [
  {
    ignores: ["**/node_modules", "**/dist", "**/.cache"],
  },
  {
    plugins: { "react-hooks": reactHooks },
    rules: {
      "react-hooks/rules-of-hooks": "error",
      "react-hooks/exhaustive-deps": "warn",
    },
  },
  { ...compat.configs["flat/recommended"], files: ["src/**/*"] },
  eslintConfigPrettier,
  { rules: { "no-unexpected-multiline": "error" } },
  {
    plugins: {
      "@typescript-eslint": tseslint.plugin,
    },

    languageOptions: {
      parser: tseslint.parser,
    },

    rules: {
      eqeqeq: "error",
      "@typescript-eslint/ban-ts-comment": "error",
    },

    files: ["**/*.ts", "**/*.tsx"],
  },
]
