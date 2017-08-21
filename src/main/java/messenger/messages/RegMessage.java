package messenger.messages;

public class RegMessage extends Message {
    private Long id;
    private String login;
    private String password;
    private String firstName;
    private String lastName;

    public RegMessage(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public void setFullName(String firstname, String lastname) {
        this.firstName = firstname;
        this.lastName = lastname;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPassword() {
        return password;
    }

    public String getLogin() {
        return login;
    }
}
