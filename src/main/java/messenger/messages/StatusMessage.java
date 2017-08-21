package messenger.messages;

import java.io.Serializable;

public class StatusMessage implements Serializable {
    private String infoString;

    public String getStatus() {
        return infoString;
    }

    public void setStatus(String infoString) {
        this.infoString = infoString;
    }
}
