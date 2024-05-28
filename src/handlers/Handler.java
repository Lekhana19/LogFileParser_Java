package handlers;

public interface Handler {
    void setNext(Handler handler);
    void handle(String log);
    void final_output();
}