#!/bin/bash

ARCHIVO_ORIGEN="/home/oier/share/kontenedoreak/apache2/www/vigicloud/index.html"
ARCHIVO_DESTINO="/home/user/partekatuak/kontenedoreak/apache2/www/vigilab/index.html"

if [ -f "$ARCHIVO_ORIGEN" ]; then
    cp "$ARCHIVO_ORIGEN" "$ARCHIVO_DESTINO"
    systemctl reload apache2
    echo "[$(date)] Archivo copiado de $ARCHIVO_ORIGEN a $ARCHIVO_DESTINO y Apache recargado" >> /home/oier/log_copia.txt
else
    echo "[$(date)] El archivo origen no existe: $ARCHIVO_ORIGEN" >> /home/oier/log_copia.txt
fi
