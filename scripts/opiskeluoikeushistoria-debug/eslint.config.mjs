import js from "@eslint/js";
import { defineConfig } from "eslint/config";
import eslintConfigPrettier from "eslint-config-prettier";
import importX from "eslint-plugin-import-x";
import globals from "globals";
import tseslint from "typescript-eslint";

export default defineConfig([
  { ignores: ["**/node_modules"] },
  js.configs.recommended,
  {
    files: ["index.js"],
    extends: [tseslint.configs.recommendedTypeCheckedOnly],
    languageOptions: {
      sourceType: "commonjs",
      globals: globals.node,
      parser: tseslint.parser,
      parserOptions: {
        projectService: true,
        tsconfigRootDir: import.meta.dirname,
      },
    },
  },
  { rules: { eqeqeq: "error" } },
  {
    plugins: { "import-x": importX },
    rules: {
      "import-x/no-extraneous-dependencies": [
        "error",
        { devDependencies: false },
      ],
    },
  },
  eslintConfigPrettier,
]);
