package cn.edu.bjfu.nekocafe.common;

public class Result<T> {
    private Integer code;     // 状态码：0为成功，非0为失败
    private String message;   // 提示信息
    private T data;           // 具体的业务数据

    // --- 下面是手动补充的 Getter 和 Setter ---
    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
    // ----------------------------------------

    // 成功时的快捷返回方法
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setMessage("success");
        result.setData(data);
        return result;
    }

    // 成功但不带数据的快捷返回方法
    public static Result<?> success() {
        return success(null);
    }

    // 失败时的快捷返回方法
    public static Result<?> error(Integer code, String message) {
        Result<?> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}