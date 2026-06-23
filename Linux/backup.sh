#!/bin/bash

now=$(date +%Y%m%d_%H%M)
backup_dir="/home/user/backups"
file="smb_backup"
extension=".tar.bz2"
source_dir="/home/oier/share"
incremental_file="$backup_dir/incremental.snar"

tar -jcPf $backup_dir/$file$now$extension --listed-incremental=$incremental_file $source_dir

echo "Incremental backup completed: $backup_dir/$file$now$extension"
