#!/usr/bin/env bash
set -e

cd "$(dirname "$0")"
rm -f lab-equipment-system-source.zip
zip -r lab-equipment-system-source.zip \
  pom.xml README.md run.sh package-source.sh .gitignore \
  READ_00_DOCUMENT_MAP.md READ_01_PROJECT_OVERVIEW.md READ_02_MEMBER_RESPONSIBILITIES.md \
  READ_03_VIVA_QA.md READ_04_FRONTEND_UI_GUIDE.md READ_05_SUBMISSION_AND_REPORT_GUIDE.md \
  READ_CN_00_文档地图.md READ_CN_01_项目概览.md READ_CN_02_组员分工.md \
  READ_CN_03_答辩问答.md READ_CN_04_Web前端说明.md READ_CN_05_提交和报告指南.md \
  src docs \
  -x "*/target/*" "*.mv.db" "*.trace.db" ".DS_Store"

echo "Created lab-equipment-system-source.zip"
