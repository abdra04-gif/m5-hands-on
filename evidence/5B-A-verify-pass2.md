# 5B Part A — verification, pass 2

Input: the fixed JavaDoc (the flagged sentence was replaced) and the same
method body.

Prompt: same as pass 1.

Reply:

- "Only `customerId` and `amount` are read from the request body; any `id`
  or `status` it carries is ignored": consistent. The body only calls
  `body.customerId()` and `body.amount()`.
- "The id and status of the returned order are those assigned by
  `OrderService#create(String, BigDecimal)`": consistent. The method
  returns `OrderDto.from(o)`, where `o = service.create(...)`, and does not
  modify `o` in between.
- The other statements (invalid input → status not exception, `@param`
  constraints, `@return` 201/400, no `@throws`) are unchanged from pass 1,
  where they were found consistent.

Result: **no inconsistencies.**

Extra checks, done by hand rather than by the AI:

- `javadoc -Xdoclint:all` reports no warnings on `create`. The only
  `OrderApi` warnings are 5 × "no comment", on the class, the constructor and
  the three endpoints the handout does not ask to document.
- A scratch program called `create` directly (output in PROMPTS.md): a valid
  body returns 201 with a UUID id and status `NEW`. Amount 0, amount −1, a
  blank `customerId` and a `null` body each return 400.
