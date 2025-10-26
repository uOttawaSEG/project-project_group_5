package ca.uottawa.seg.otams;

import androidx.annotation.NonNull;

public enum RegistrationStatus
{
    PENDING,
    REJECTED,
    APPROVED;

    @NonNull
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }
}
