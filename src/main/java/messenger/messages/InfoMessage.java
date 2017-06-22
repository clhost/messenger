package messenger.messages;

/**
 * @author clhost
 */
public class InfoMessage extends Message {
    private Long user_id;

    public void setId(Long id) {
        user_id = id;
    }

    public Long getId() {
        return user_id;
    }
}
