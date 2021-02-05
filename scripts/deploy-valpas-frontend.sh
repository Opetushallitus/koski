#!/bin/bash
set -o errexit -o nounset -o pipefail

ENV="${1:-"dev"}"
AWS_CLI="$(which aws)"
DIST_PATH="$(pwd)/valpas-web/dist"
S3_BUCKET="valpas-frontend-$ENV"

deploy() {
  local source_dir="$1"
  local s3_bucket="$2"
  local env="$3"

  local aws="$AWS_CLI --profile oph-koski-$ENV"
  local s3_path="valpas/virkailija"
  local distribution_id_parameter_name='/valpas/cloudfront-distribution-id'

  echo "Deploying $source_dir to S3 bucket $s3_bucket (environment: $env)"

  echo "Syncing data to bucket"
  $aws s3 sync "$source_dir" "s3://$s3_bucket/$s3_path" --delete

  echo "Invalidating CloudFront distribution caches"
  local distribution_id
  distribution_id=$($aws ssm get-parameter --name "$distribution_id_parameter_name" --query Parameter.Value --output text)
  local invalidation_id
  invalidation_id=$($aws cloudfront create-invalidation --distribution-id "$distribution_id" --paths '/*' --query Invalidation.Id --output text)

  echo "Waiting for invalidation to complete"
  $aws cloudfront wait invalidation-completed --distribution-id "$distribution_id" --id "$invalidation_id"

  echo "Done!"
}

deploy "$DIST_PATH" "$S3_BUCKET" "$ENV"
