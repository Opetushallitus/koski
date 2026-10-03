import js from "@eslint/js";
import { defineConfig } from "eslint/config";
import eslintConfigPrettier from "eslint-config-prettier";
import globals from "globals";

export default defineConfig([
  { ignores: ["**/node_modules"] },
  js.configs.recommended,
  {
    files: ["index.js"],
    languageOptions: { sourceType: "commonjs", globals: globals.node },
  },
  { rules: { eqeqeq: "error" } },
  eslintConfigPrettier,
]);
