package messenger.store.datasets;

import messenger.messages.TextMessage;

import java.util.ArrayList;
import java.util.List;

public class Chat {
    private Long id;
    private ArrayList<TextMessage> messages;
    private List<Long> participantIds;
    private Long adminId;
    private String chatName;

    public Chat() {
        messages = new ArrayList<>();
        participantIds = new ArrayList<>();
    }

    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public void addMessage(TextMessage message) {
        messages.add(message);
    }

    public List<Long> getParticipantIds() {
        return participantIds;
    }

    public void setParticipantIds(List<Long> participantIds) {
        this.participantIds = participantIds;
    }

    public void setParticipant(Long id) {
        participantIds.add(id);
    }

    public Long getAdminId() {
        return adminId;
    }

    public void setAdminId(long adminId) {
        this.adminId = adminId;
    }

    public ArrayList<TextMessage> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<TextMessage> messages) {
        this.messages = messages;
    }

    public String getChatName() {
        return chatName;
    }

    public void setChatName(String chatName) {
        this.chatName = chatName;
    }
}
