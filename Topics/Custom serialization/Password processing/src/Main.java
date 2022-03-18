import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

class UserProfile implements Serializable {
    private static final long serialVersionUID = 26292552485L;

    private String login;
    private String email;
    private transient String password;

    public UserProfile(String login, String email, String password) {
        this.login = login;
        this.password = password;
        this.email = email;
    }

    private void writeObject(ObjectOutputStream objectOutputStream) throws IOException {
        objectOutputStream.defaultWriteObject();
        String newPass = encript(password);
        objectOutputStream.writeObject(newPass);
    }

    private void readObject(ObjectInputStream objectInputStream) throws IOException, ClassNotFoundException {
        objectInputStream.defaultReadObject();
        this.password = decript((String) objectInputStream.readObject());
    }

    private String encript(String password) {
        String newPassword = "";
        char[] pass = password.toCharArray();
        for (int i = 0; i < pass.length; i++) {
            pass[i] = (char) (pass[i] + 1);
            newPassword += pass[i];
        }
        return newPassword;
    }

    private String decript(String password) {
        String newPassword = "";
        char[] pass = password.toCharArray();
        for (int i = 0; i < pass.length; i++) {
            pass[i] = (char) (pass[i] - 1);
            newPassword += pass[i];
        }
        return newPassword;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}