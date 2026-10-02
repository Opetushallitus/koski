import js from '@eslint/js'
import { defineConfig } from 'eslint/config'
import tseslint from 'typescript-eslint'
import eslintConfigPrettier from 'eslint-config-prettier'

export default defineConfig([
  { ignores: ['**/node_modules', '**/dist'] },
  js.configs.recommended,
  { files: ['**/*.ts'], extends: [tseslint.configs.recommended] },
  { rules: { eqeqeq: 'warn' } },
  eslintConfigPrettier
])
