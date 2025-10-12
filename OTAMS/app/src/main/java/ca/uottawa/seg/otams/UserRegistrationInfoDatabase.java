package ca.uottawa.seg.otams;

import java.util.Objects;

public class UserRegistrationInfoDatabase {
    // Class to help read and write to the users tree of the database

    // All possible variables that can be stored for a user
    String firstName;
    String lastName;
    String email;
    String password;
    String phoneNumber;
    String program = null; // Exclusive to students

    String highestDegree = null; // Exclusive to tutors

    String coursesOffered = null; // Exclusive to tutors

    String typeOfUser; // Determines whether the user is a student or tutor

    // Empty constructor to avoid errors in Firebase
    public UserRegistrationInfoDatabase() {}

    // Constructor
    public UserRegistrationInfoDatabase(String firstName, String lastName, String email, String password, String phoneNumber, String program, String highestDegree, String coursesOffered, String typeOfUser) {
        // Setting variables shared by both students and tutors
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.typeOfUser = typeOfUser;

        if (typeOfUser.equals("Student")) {
            // Setting value of variable exclusive to students
            this.program = program;
        } else if (typeOfUser.equals("Tutor")) {
            // Setting value of variable exclusive to tutors
            this.highestDegree = highestDegree;
            this.coursesOffered = coursesOffered;
        }
    }

    // Getter and setter methods for each variable
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

    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }

    public String getHighestDegree() {
        return highestDegree;
    }

    public void setHighestDegree(String highestDegree) {
        this.highestDegree = highestDegree;
    }

    public String getCoursesOffered() {
        return coursesOffered;
    }

    public void setCoursesOffered(String coursesOffered) {
        this.coursesOffered = coursesOffered;
    }

    public String getTypeOfUser() {
        return typeOfUser;
    }

    public void setTypeOfUser(String typeOfUser) {
        this.typeOfUser = typeOfUser;
    }
}
