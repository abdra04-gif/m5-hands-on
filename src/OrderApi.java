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
