#!/usr/bin/env bash
set -euo pipefail

# Editable defaults
RABBIT_HOST="localhost:15672"
RABBIT_USER="ledger"
RABBIT_PASS="ledger"
VHOST="%2F"            # the default vhost "/", URL-encoded
EXCHANGE="payments"
ROUTING_KEY="payment.settled"

# Arguments, all optional:
#   1: paymentId   2: merchantAccountId   3: amount   4: currency
PAYMENT_ID="${1:-p-$(date +%s)}"
MERCHANT_ACCOUNT_ID="${2:-7f3c9a2e-4b1d-4e8a-9c63-0d5b7e214a98}"
AMOUNT="${3:-125.50}"
CURRENCY="${4:-EUR}"

# A new eventId on every call, like a redelivery under a new event id
EVENT_ID="$(uuidgen | tr '[:upper:]' '[:lower:]')"

PAYLOAD=$(printf '{"eventId":"%s","paymentId":"%s","merchantAccountId":"%s","amount":"%s","currency":"%s"}' \
  "$EVENT_ID" "$PAYMENT_ID" "$MERCHANT_ACCOUNT_ID" "$AMOUNT" "$CURRENCY")

# The payload is a string inside the JSON body, so its quotes must be escaped
ESCAPED_PAYLOAD=${PAYLOAD//\"/\\\"}

echo "Publishing: $PAYLOAD"

RESPONSE=$(curl -sS -u "$RABBIT_USER:$RABBIT_PASS" \
  -H "Content-Type: application/json" \
  -X POST "http://$RABBIT_HOST/api/exchanges/$VHOST/$EXCHANGE/publish" \
  -d "{\"properties\":{\"content_type\":\"application/json\"},\"routing_key\":\"$ROUTING_KEY\",\"payload\":\"$ESCAPED_PAYLOAD\",\"payload_encoding\":\"string\"}")

echo "$RESPONSE"