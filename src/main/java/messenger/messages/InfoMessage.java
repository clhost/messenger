package messenger.messages;

/**
 * @author clhost
 */
public class InfoMessage extends Message {
    private Long user_id;

    public void setUserId(Long id) {
        user_id = id;
    }

    public Long getUserId() {
        return user_id;
    }
}
