package ca.uottawa.seg.otams;

import androidx.annotation.NonNull;

public enum SessionStatus {
    OPEN,        // Tutor created the slot; no student booked yet
    PENDING,     // Student requested the slot, waiting for tutor approval
    APPROVED,    // Tutor approved; booked session
    REJECTED,    // Tutor rejected the student’s request; dissapears from tutor dashboard
    COMPLETED;   // The session took place (past sessions)

    // CANCELLED,   // Either tutor or student cancelled after approval; probably not needed, will just remove the student info when cancelled and change the status back to open
    // DELETED;     // Tutor removed an open slot; probably not needed

    @NonNull
    @Override
    public String toString() {
        return name().toUpperCase();
    }
}

// All Sessions - Shows all slots with delete button beside each and arrow to see more info only if it has been booked or is attempting to be booked
// Pending - Approved, rejected (session must be rejected before deleting)
// Upcoming Sessions - (Approved) Cancel option
// Past Session