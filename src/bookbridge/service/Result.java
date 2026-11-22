
package bookbridge.service;

public class Result<T> {

    private final T value;
    private final String message;

    private Result(T value, String message) {
        this.value = value;
        this.message = message;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(value, "Operation successful");
    }

    public T getValue() {
        return value;
    }

    public String getMessage() {
        return message;
    }
}
