#!/bin/bash

now=$(date +%Y%m%d_%H%M)
backup_dir="/home/anartz/"
file="${backup_dir}backup_osoa_"
extension=".tar.bz2"

tar -jcPf "${file}${now}${extension}" /home/anartz/share

echo "Backup osoa eginda | --> | ${file}${now}${extension}"
