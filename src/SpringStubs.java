import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// Fake Spring Web annotations and ResponseEntity so that OrderApi compiles
// without Spring on the classpath. They mirror the names and shapes of
// org.springframework.web.bind.annotation.* / org.springframework.http.*
// but carry no behaviour.

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.TYPE)
@interface RestController {}

@Retention(RetentionPolicy.RUNTIME) @Target({ElementType.TYPE, ElementType.METHOD})
@interface RequestMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface GetMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface PostMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
@interface DeleteMapping { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
@interface PathVariable { String value() default ""; }

@Retention(RetentionPolicy.RUNTIME) @Target(ElementType.PARAMETER)
@interface RequestBody {}

final class ResponseEntity<T> {
    private final int status;
    private final T body;

    private ResponseEntity(int status, T body) {
        this.status = status;
        this.body = body;
    }

    public int getStatusCode() { return status; }
    public T getBody() { return body; }

    static <T> ResponseEntity<T> ok(T body) { return new ResponseEntity<>(200, body); }
    static Builder status(int status) { return new Builder(status); }
    static Builder badRequest() { return new Builder(400); }
    static Builder notFound() { return new Builder(404); }
    static Builder noContent() { return new Builder(204); }

    static final class Builder {
        private final int status;
        private Builder(int status) { this.status = status; }
        <T> ResponseEntity<T> body(T body) { return new ResponseEntity<>(status, body); }
        <T> ResponseEntity<T> build() { return new ResponseEntity<>(status, null); }
    }
}
