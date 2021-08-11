#!/usr/bin/env bash

# remove v from the tag if present
VERSION=${BUILD_TAG//v/}

echo "pushing solr package to s3"

aws s3 cp $GITHUB_WORKSPACE/solr/package s3://${S3_PATH}/solr/dist/$VERSION --recursive

echo "successfully pushed package to s3"
