package ca.uottawa.seg.otams;

public abstract class User {
    //Class variables
    private String role; // Determines whether the user is a student or tutor

    //Instance variables
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String phoneNumber;
    private String requestStatus; // Determines the status of the user's registration request (pending, rejected, approved)

    // Empty constructor to avoid errors in Firebase
    public User() {}

    // Constructor
    public User(String role, String firstName, String lastName, String email, String password, String phoneNumber) {
        // Setting variables shared by both students and tutors
        this.role = role;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;

        requestStatus = "pending"; // Request always starts as pending when created
    }

    // Getter and setter methods for each variable
    public String getRole() {
        return role;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(String requestStatus) {
        this.requestStatus = requestStatus;
    }
}