// Reconstructed for Session 5B: OrderApi.java is described by the handout and
// the starter README but is missing from m5-pub.tgz. Four endpoints, one per
// OrderService method; annotations come from SpringStubs.java. See PROMPTS.md.

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderApi {
    private final OrderService service;

    public OrderApi(OrderService service) {
        this.service = service;
    }

    @GetMapping
    public List<OrderDto> list() {
        return service.listAll().stream().map(OrderDto::from).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> get(@PathVariable("id") String id) {
        return service.findById(id)
                .map(o -> ResponseEntity.ok(OrderDto.from(o)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Create an order for a customer ({@code POST /orders}).
     *
     * <p>Only {@code customerId} and {@code amount} are read from the request
     * body; any {@code id} or {@code status} it carries is ignored. The id and
     * status of the returned order are those assigned by
     * {@link OrderService#create(String, java.math.BigDecimal)}.
     *
     * <p>Invalid input is reported through the response status, not by an exception.
     *
     * <p>TODO: currency, scale and upper bound of {@code amount} are not
     * constrained by the code.
     *
     * @param body the order to create, as an {@link OrderDto}; may be {@code null}
     *             (rejected). {@code customerId} must be non-null and not blank;
     *             {@code amount} must be non-null and strictly greater than zero
     * @return {@code 201 Created} with the created order as an {@link OrderDto};
     *         {@code 400 Bad Request} with an empty body if {@code body} is
     *         {@code null} or violates the constraints above
     */
    @PostMapping
    public ResponseEntity<OrderDto> create(@RequestBody OrderDto body) {
        if (body == null
                || body.customerId() == null || body.customerId().isBlank()
                || body.amount() == null || body.amount().signum() <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Order o = service.create(body.customerId(), body.amount());
        return ResponseEntity.status(201).body(OrderDto.from(o));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(@PathVariable("id") String id) {
        Optional<Order> existing = service.findById(id);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!service.cancel(id)) {
            return ResponseEntity.status(409).build();
        }
        return ResponseEntity.noContent().build();
    }
}
