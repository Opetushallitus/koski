import path from 'node:path'
import { fileURLToPath } from 'node:url'
import js from '@eslint/js'
import tseslint from '@typescript-eslint/eslint-plugin'
import tsParser from '@typescript-eslint/parser'
import { fixupConfigRules } from '@eslint/compat'
import prettier from 'eslint-plugin-prettier'
import eslintConfigPrettier from 'eslint-config-prettier'

const tsconfigRootDir = path.dirname(fileURLToPath(import.meta.url))

const tsRules = tseslint.configs['flat/recommended'].map((config) => ({
  ...config,
  files: ['**/*.ts'],
  languageOptions: {
    ...config.languageOptions,
    parser: tsParser,
    parserOptions: {
      ...config.languageOptions?.parserOptions,
      tsconfigRootDir
    }
  },
  plugins: {
    ...config.plugins,
    prettier
  },
  rules: {
    ...config.rules,
    'prettier/prettier': 'error',
    'no-undef': 'off'
  }
}))

export default [
  { ignores: ['**/node_modules', '**/dist'] },
  ...fixupConfigRules([js.configs.recommended, eslintConfigPrettier]),
  ...tsRules
]
