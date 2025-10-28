package ca.uottawa.seg.otams;

public class Student extends User {
    //Class variables
    public static String role = "Student";

    // Instance variable
    public String program;

    public Student() {}

    public Student(String firstName, String lastName, String email, String password, String phoneNumber, String program) {
        super("Student", firstName, lastName, email, password, phoneNumber);

        this.program = program;
    }

    // Getter and setter methods for program variable
    public String getProgram() {
        return program;
    }

    public void setProgram(String program) {
        this.program = program;
    }
}