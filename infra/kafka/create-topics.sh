#!/usr/bin/env bash
# Creates every topic up front. The broker runs with auto.create.topics.enable=false, so a
# typo in a topic name fails loudly instead of silently creating a new, unread topic.
#
# Each DLQ has the same partition count as its source topic: Spring Kafka's
# DeadLetterPublishingRecoverer sends a failed record to the same partition number on the
# DLQ, which must therefore exist.
set -euo pipefail

BOOTSTRAP_SERVER=${BOOTSTRAP_SERVER:-kafka:19092}
PARTITIONS=${PARTITIONS:-3}
REPLICATION_FACTOR=${REPLICATION_FACTOR:-1}

EVENT_TYPES=(
    PaymentInitiated
    ComplianceCheckPassed
    ComplianceCheckFailed
    PaymentSettled
    PaymentFailed
    PaymentRejected
)

create_topic() {
    /opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP_SERVER" \
        --create --if-not-exists \
        --topic "$1" \
        --partitions "$PARTITIONS" \
        --replication-factor "$REPLICATION_FACTOR"
}

for event_type in "${EVENT_TYPES[@]}"; do
    create_topic "payment.events.${event_type}"
    create_topic "dlq.payment.events.${event_type}"
done

echo "Topics on ${BOOTSTRAP_SERVER}:"
/opt/kafka/bin/kafka-topics.sh --bootstrap-server "$BOOTSTRAP_SERVER" --list
