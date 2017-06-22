package messenger.messages;

/**
 * @author clhost
 */
public class RegMessage extends Message {
    private Long id;
    private String login;
    private String password;
    private String firstname;
    private String lastname;

    public RegMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void setFullName(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }
}
