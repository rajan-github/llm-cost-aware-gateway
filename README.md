# llm-cost-aware-gateway

A middleware service that sits between client applications and LLM APIs to predict usage, enforce budgets, and optimize request routing in real time.

This system uses probabilistic token estimation (p50/p95) to forecast request cost before execution, enabling proactive budget enforcement. A dual-layer architecture combines Redis-based atomic counters for low-latency admission control with an append-only PostgreSQL ledger for strong consistency and auditability.

When budget constraints are detected, the gateway applies intelligent degradation strategies such as model downgrading, context truncation, and output limiting—ensuring graceful performance under cost pressure instead of hard failures.

Key capabilities include:

Token usage prediction using historical statistics
Multi-tenant budget tracking and enforcement
Real-time admission control with Redis
Strongly consistent usage ledger (append-only)
Intelligent request routing and degradation
Retry safety, idempotency, and cancellation-aware accounting

This project demonstrates patterns from distributed systems such as admission control, eventual consistency with reconciliation, and cost-aware resource allocation, inspired by principles from Designing Data-Intensive Applications.
