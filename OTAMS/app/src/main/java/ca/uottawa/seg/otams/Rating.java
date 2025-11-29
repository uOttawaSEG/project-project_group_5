package ca.uottawa.seg.otams;

public class Rating {
    private String id;
    private String tutorPhoneNumber;
    private String studentPhoneNumber;
    private String sessionId;
    private int ratingValue; // 1-5 stars
    private long timestamp;

    public Rating() {
        // Empty constructor for Firebase
    }

    public Rating(String id, String tutorPhoneNumber, String studentPhoneNumber, String sessionId, int ratingValue, long timestamp) {
        this.id = id;
        this.tutorPhoneNumber = tutorPhoneNumber;
        this.studentPhoneNumber = studentPhoneNumber;
        this.sessionId = sessionId;
        this.ratingValue = ratingValue;
        this.timestamp = timestamp;
    }

    // Getter and setter methods
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTutorPhoneNumber() {
        return tutorPhoneNumber;
    }

    public void setTutorPhoneNumber(String tutorPhoneNumber) {
        this.tutorPhoneNumber = tutorPhoneNumber;
    }

    public String getStudentPhoneNumber() {
        return studentPhoneNumber;
    }

    public void setStudentPhoneNumber(String studentPhoneNumber) {
        this.studentPhoneNumber = studentPhoneNumber;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public int getRatingValue() {
        return ratingValue;
    }

    public void setRatingValue(int ratingValue) {
        this.ratingValue = ratingValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}