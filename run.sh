#!/usr/bin/env bash
set -e

if [ -z "$DB_PASSWORD" ]; then
  echo "Please run with your MySQL root password:"
  echo "  DB_PASSWORD='your_password' ./run.sh"
  exit 1
fi

mvn exec:java
