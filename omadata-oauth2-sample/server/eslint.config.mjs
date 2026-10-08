import js from '@eslint/js'
import { defineConfig } from 'eslint/config'
import tseslint from 'typescript-eslint'
import eslintConfigPrettier from 'eslint-config-prettier'
import importX from 'eslint-plugin-import-x'

export default defineConfig([
  { ignores: ['**/node_modules', '**/dist'] },
  js.configs.recommended,
  {
    files: ['**/*.ts'],
    extends: [tseslint.configs.recommendedTypeChecked],
    languageOptions: {
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname
      }
    },
    rules: {
      '@typescript-eslint/consistent-type-imports': [
        'error',
        { fixStyle: 'inline-type-imports' }
      ],
      '@typescript-eslint/no-import-type-side-effects': 'error'
    }
  },
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
