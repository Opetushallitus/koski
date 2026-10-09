import js from '@eslint/js'
import { defineConfig } from 'eslint/config'
import compat from 'eslint-plugin-compat'
import eslintConfigPrettier from 'eslint-config-prettier'
import importX from 'eslint-plugin-import-x'
import globals from 'globals'
import tseslint from 'typescript-eslint'

export default defineConfig([
  { ignores: ['vendor'] },
  js.configs.recommended,
  {
    files: ['src/**/*.js'],
    languageOptions: {
      sourceType: 'commonjs',
      globals: globals.browser
    }
  },
  {
    files: ['webpack.config.mts'],
    extends: [tseslint.configs.recommended],
    languageOptions: { globals: globals.node }
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
  {
    plugins: { 'import-x': importX },
    rules: { 'import-x/no-extraneous-dependencies': 'error' }
  },
  {
    files: ['src/**/*'],
    rules: {
      'import-x/no-extraneous-dependencies': [
        'error',
        { devDependencies: false }
      ]
    }
  },
  eslintConfigPrettier,
  { rules: { 'no-unexpected-multiline': 'error' } }
])
