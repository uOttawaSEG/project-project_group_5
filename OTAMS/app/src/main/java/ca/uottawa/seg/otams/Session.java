package ca.uottawa.seg.otams;// --- Inside your Session.java ---
import java.util.Calendar;
import java.util.Date;
import com.google.firebase.database.Exclude;

import ca.uottawa.seg.otams.SessionStatus;

public class Session {

    private Object startTime; // Firebase stores as Map
    private Object endTime;

    // Optional if you already have other fields:
    private String id;
    private String tutorPhoneNumber;
    private String studentPhoneNumber;
    private SessionStatus sessionStatus;
    private String date;
    private String tutorName;
    private String studentName;
    private boolean autoApprove;

    // Empty constructor required for Firebase
    public Session() { }

    // your normal constructor (you can keep your current one)
    public Session(String id, String date, Date startTime, Date endTime,
                   String tutorName, String tutorPhoneNumber,
                   String studentName, String studentPhoneNumber,
                   SessionStatus sessionStatus, boolean autoApprove) {
        this.id = id;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.tutorName = tutorName;
        this.tutorPhoneNumber = tutorPhoneNumber;
        this.studentName = studentName;
        this.studentPhoneNumber = studentPhoneNumber;
        this.sessionStatus = sessionStatus;
        this.autoApprove = autoApprove;
    }

    // ✅ Custom logic to convert Firebase's weird date map into a real Date
    @Exclude
    public Date getStartTime() {
        return parseFirebaseDateObject(startTime);
    }

    public void setStartTime(Date date) {
        this.startTime = date;
    }

    @Exclude
    public Date getEndTime() {
        return parseFirebaseDateObject(endTime);
    }

    public void setEndTime(Date date) {
        this.endTime = date;
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

    public String getSessionStatus(){ return this.sessionStatus.toString(); }

    public void setSessionStatus(SessionStatus sessionStatus){ this.sessionStatus = sessionStatus; }

    public boolean getAutoApprove(){ return this.autoApprove; }

    public void setAutoApprove(boolean autoApprove){ this.autoApprove = autoApprove; }

    // --- helper to convert firebase date maps into proper Date objects ---
    private Date parseFirebaseDateObject(Object obj) {
        if (obj == null) return null;

        // Case 1: Firebase stored as map of fields
        if (obj instanceof java.util.Map) {
            try {
                java.util.Map<?, ?> map = (java.util.Map<?, ?>) obj;

                if (map.containsKey("time")) {
                    long millis = ((Number) map.get("time")).longValue();
                    return new Date(millis);
                }

                Number year = (Number) map.get("year");
                Number month = (Number) map.get("month");
                Number day = (Number) map.get("date");
                Number hour = (Number) map.get("hours");
                Number minute = (Number) map.get("minutes");
                Number second = (Number) map.get("seconds");

                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.YEAR, 1900 + (year != null ? year.intValue() : 0));
                cal.set(Calendar.MONTH, month != null ? month.intValue() : 0);
                cal.set(Calendar.DAY_OF_MONTH, day != null ? day.intValue() : 1);
                cal.set(Calendar.HOUR_OF_DAY, hour != null ? hour.intValue() : 0);
                cal.set(Calendar.MINUTE, minute != null ? minute.intValue() : 0);
                cal.set(Calendar.SECOND, second != null ? second.intValue() : 0);
                cal.set(Calendar.MILLISECOND, 0);
                return cal.getTime();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        // Case 2: Firebase somehow returned directly as timestamp (rare)
        if (obj instanceof Long) {
            return new Date((Long) obj);
        }

        // Case 3: Already a Date (e.g. local use)
        if (obj instanceof Date) {
            return (Date) obj;
        }

        return null;
    }

}