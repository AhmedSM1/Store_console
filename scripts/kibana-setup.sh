#!/bin/sh

echo "Waiting for Kibana to be ready..."

# Loop until Kibana API returns an 'available' status
until curl -s http://kibana:5601/api/status | grep -q '\"level\":\"available\"'; do
  echo "Kibana is still starting up... sleeping 5s"
  sleep 5
done

echo "Kibana is up! Creating Data View..."

# Create the Data View via Kibana API
RESPONSE=$(curl -s -X POST "http://kibana:5601/api/data_views/data_view" \
  -H "Content-Type: application/json" \
  -H "kbn-xsrf: true" \
  -d '{
    "data_view": {
       "title": "microservices-logs-*",
       "name": "Microservices Logs",
       "timeFieldName": "@timestamp"
    }
  }')

if echo "$RESPONSE" | grep -q "data_view"; then
  echo "Successfully created Data View: Microservices Logs"
else
  echo "Failed to create Data View or it already exists."
fi

echo "Setup complete!"