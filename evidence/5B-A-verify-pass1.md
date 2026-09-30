# 5B Part A — verification, pass 1

Input: the JavaDoc at commit `6a76dbb` (pass 1) and the body of `OrderApi#create`.

Prompt:

```
Given the JavaDoc above and the method body below,
identify any statement in the JavaDoc that is inconsistent
with the code. Do not fix; just list.
```

Reply:

1. "The new order is assigned a server-generated UUID and starts in status
   `NEW`": the method body neither generates an id nor sets a status. It
   calls `service.create(customerId, amount)` and returns
   `OrderDto.from(o)`. Nothing in this method says the id is a UUID or that
   the status is `NEW`. Under the rule "if any behaviour is not evident from
   the code, write TODO", this statement should not be there.

No other inconsistencies:

- "any `id` or `status` in the request body is ignored": consistent. Only
  `body.customerId()` and `body.amount()` are read.
- "Invalid input is reported through the response status, not by an
  exception": consistent. Every validation failure returns
  `ResponseEntity.badRequest().build()`, and nothing is thrown.
- `@param`: consistent. `body == null` → 400; `customerId() == null ||
  isBlank()` → 400; `amount() == null || signum() <= 0` → 400, so zero is
  rejected ("strictly greater than zero").
- `@return`: consistent. Success → `status(201).body(OrderDto.from(o))`;
  failure → `badRequest().build()` (empty body).
- No `@throws`: consistent. The method declares no exceptions and throws none.

Result: **1 inconsistency.**
