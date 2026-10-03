import js from '@eslint/js'
import { defineConfig } from 'eslint/config'
import compat from 'eslint-plugin-compat'
import eslintConfigPrettier from 'eslint-config-prettier'
import globals from 'globals'
import tseslint from 'typescript-eslint'

export default defineConfig([
  { ignores: ['**/node_modules', 'vendor'] },
  js.configs.recommended,
  {
    files: ['src/**/*.js'],
    languageOptions: {
      sourceType: 'commonjs',
      globals: globals.browser
    }
  },
  {
    files: ['webpack.config.js'],
    languageOptions: {
      sourceType: 'commonjs',
      globals: globals.node
    }
  },
  {
    files: ['**/*.ts'],
    extends: [tseslint.configs.recommendedTypeChecked],
    languageOptions: {
      globals: globals.browser,
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname
      }
    }
  },
  { ...compat.configs['flat/recommended'], files: ['src/**/*'] },
  { rules: { eqeqeq: 'error' } },
  eslintConfigPrettier,
  { rules: { 'no-unexpected-multiline': 'error' } }
])
