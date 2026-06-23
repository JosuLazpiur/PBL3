#!/bin/bash

NONDIK="/home/user/share"
NORA="/home/user/windows_backups"
DATA=$(date +%Y-%m-%d_%H-%M-%S)

rm -f "$NORA/backup.tar.gz"

tar -czf "$NORA/backup_$DATA.tar.gz" -C "$NONDIK" .

echo "Backup-a ondo atera da"
