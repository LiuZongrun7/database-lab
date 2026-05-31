#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
output_dir="${OUTPUT_DIR:-../lab-equipment-system-docs/generated}"
mkdir -p "$output_dir"
output_file="$output_dir/lab-equipment-system-source.zip"

rm -f "$output_file"
zip -r "$output_file" \
  pom.xml run.sh package-source.sh .gitignore src \
  -x "*/target/*" "*.mv.db" "*.trace.db" ".DS_Store"

echo "Created $output_file with code files only"
