#!/usr/bin/env bash
# This script command downloads the results
curl -X GET -H "Content-Type: application/json" -H "Accept: application/json" http://localhost/analyses/$1
