#!/bin/bash

INTERFACE="end0"
LOG_FILE="/data/arp-scan.log"

while true; do
    echo "=== $(date) ===" >> "$LOG_FILE"
    arp-scan --interface="$INTERFACE" --local --plain >> "$LOG_FILE" 2>&1
    echo "" >> "$LOG_FILE"
    sleep 10
done