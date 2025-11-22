package ca.uottawa.seg.otams;

public class Tutor extends User {
    // Instance variables
    private String highestDegree;
    private String coursesOffered;
    private int rating;

    public Tutor() {}

    public Tutor(String firstName, String lastName, String email, String password, String phoneNumber, String highestDegree, String coursesOffered) {
        super("Tutor", firstName, lastName, email, password, phoneNumber);

        this.highestDegree = highestDegree;
        this.coursesOffered = coursesOffered;
        this.rating = 2;
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

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

}