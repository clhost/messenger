package messenger.messages;

import java.util.ArrayList;

/**
 * @author clhost
 */
public class ChatCreateMessage extends Message {
    private ArrayList<Long> participants;
    private Long creator_id;

    public ChatCreateMessage() {
        participants = new ArrayList<>();
    }

    public ArrayList<Long> getParticipants() {
        return participants;
    }

    public void setParticipants(ArrayList<Long> list) {
        participants.addAll(list);
    }

    public void setParticipants(String[] tokens) {
        for (String s : tokens) {
            participants.add(Long.parseLong(s));
        }
    }

    public Long getCreator_id() {
        return creator_id;
    }

    public void setCreator_id(Long creator_id) {
        this.creator_id = creator_id;
    }
}
