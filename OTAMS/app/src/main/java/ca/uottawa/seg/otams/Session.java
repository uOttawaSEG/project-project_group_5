package ca.uottawa.seg.otams;

import java.util.Date;

public class Session {

    // Instance variable
    private String id;
    private String date;
    private Date startTime;
    private Date endTime;
    private String tutorName;
    private String tutorPhoneNumber;
    private String studentName;
    private String studentPhoneNumber;
    private String sessionStatus;
    private Session() {}

    public Session(String id, String date, Date startTime, Date endTime, String tutorName, String tutorPhoneNumber, String studentName, String studentPhoneNumber, String registrationStatus) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutorName = tutorName;
        this.tutorPhoneNumber = tutorPhoneNumber;
        this.studentName = studentName;
        this.studentPhoneNumber = studentPhoneNumber;
        this.sessionStatus = "PENDING";
    }

    // Getter and setter methods for variables
    public String getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getTutorName() {
        return tutorName;
    }

    public void setTutorName(String tutorName) {
        this.tutorName = tutorName;
    }

    public String getTutorPhoneNumber() {
        return tutorPhoneNumber;
    }

    public void setTutorPhoneNumber(String tutorPhoneNumber) {
        this.tutorPhoneNumber = tutorPhoneNumber;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public String getStudentPhoneNumber() {
        return studentPhoneNumber;
    }

    public void setStudentPhoneNumber(String studentPhoneNumber) {
        this.studentPhoneNumber = studentPhoneNumber;
    }

    public String getSessionStatus(){ return sessionStatus; }
    public void setSessionStatus(String status){ this.sessionStatus = status; }
}
