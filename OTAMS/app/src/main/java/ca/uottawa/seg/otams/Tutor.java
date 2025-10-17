package ca.uottawa.seg.otams;

public class Tutor extends User {
    //Class variables
    // public static String role = "Tutor";

    // Instance variables
    public String highestDegree;
    public String coursesOffered;

    public Tutor() {}

    public Tutor(String firstName, String lastName, String email, String password, String phoneNumber, String highestDegree, String coursesOffered) {
        super("Tutor", firstName, lastName, email, password, phoneNumber);

        this.highestDegree = highestDegree;
        this.coursesOffered = coursesOffered;
    }

    // Getter and setter methods for variables
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
}