package ca.uottawa.seg.otams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public enum SessionStatus
{
    PENDING(null), // This is to indicate that a student is trying to book the tutor's request and the tutor has not approved or rejected it
    REJECTED(null), // This is for when a tutor rejects a request from a student to have the session with the tutor. If rejected we can change it back to an open timeslot i guess so we don't really need this check for now but it will be relevent
    // later when we need to implement the student side where they will need to know if their request got rejected or not. ALternatively I guess we can have sessionStatus enum for tutor side and sessionRequest enum for student side to deal with these things but
    // that still would mess with if a rejected session gets rebooked so idk
    APPROVED(null), // This is for when a tutor approves a session request from a student
    OPEN(null), // This is for when a session slot created by a tutor is not currently booked or trying to be booked by a tutor
    DELETED(null); // This is for when a tutor delete an availibility slot that was previously open

    // Other notes:
    /*
    A tutor can cancel a session once it's booked. When they cancel a session it becomes an open slot again.

    On the tutor dashboard we have tutor availibility slots (all open slots), upcoming sessions (which includes booked and pending sessions which
    the tutor can click to see info about the student who has booked/requested to book the session, then there is past sessions which puts sessions
    that have already occured (have been approved and happened) there. Availibility slots that are not booked before they come up should also be removed from the
    dashboard and firebase (they should not be moved to past sessions if they were never booked). So, if the tutor clicks a pending request then they should have
    the option to approve and reject. If the tutor has approved of the session they should have the option to cancel it only. If the slot is open (unbooked), the tutor should
    just have the option to delete it
     */

    private final Class<? extends AppCompatActivity> classObj;

    SessionStatus(Class<? extends AppCompatActivity> registrationPending) {
        this.classObj = registrationPending;
    }

    @NonNull
    @Override
    public String toString() {
        return this.name().toUpperCase(); // Converts enum value to a string
    }

    public Class<? extends AppCompatActivity> getStatusClass() {
        return this.classObj;
    }
}
