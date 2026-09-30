# 5B Part C — Swagger UI render check

Renderer: Swagger UI 5.33.0 (`swagger-ui-dist`), run locally in headless
Google Chrome with every operation expanded (`docExpansion: full`). The
results below were read from the rendered DOM. Screenshots:
`5B-C-swagger-ui-raw.png` (the spec as generated, commit `d93a158`) and
`5B-C-swagger-ui-final.png` (after the hand edit).

| Check | Raw (as generated) | Final |
|---|---|---|
| Renders without errors (no Swagger error panel) | yes | yes |
| Every endpoint, with the correct path and method | GET /orders, POST /orders, GET /orders/{id}, DELETE /orders/{id}: yes | same |
| Every referenced DTO defined in components (Schemas → `OrderDto`) | yes | yes |
| GET /orders: ≥1 2xx and ≥1 4xx | 200 only: **no 4xx** | 200, 406 |
| POST /orders | 201, 400 | 201, 400 |
| GET /orders/{id} | 200, 404 | 200, 404 |
| DELETE /orders/{id} | 204, 404, 409 | 204, 404, 409 |

**Missing 4xx added by hand: 1.** `406 Not Acceptable` on `GET /orders`.
`OrderApi#list` has no error path of its own; 406 is what Spring's content
negotiation returns when the `Accept` header excludes `application/json`.

Additionally checked with `openapi-spec-validator`: valid OpenAPI 3.0.3.
