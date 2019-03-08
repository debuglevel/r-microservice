#!/usr/bin/env bash
# This script uploads the upload.json
# An UUID will be printed to stdout. The upload and download script can be chained therefore:
# $ bash latex-microservice-download.sh $(bash latex-microservice-uplod.sh)
curl -X POST -d @upload.json -H "Content-Type: application/json" -H "Accept: application/json" http://localhost/analyses/
