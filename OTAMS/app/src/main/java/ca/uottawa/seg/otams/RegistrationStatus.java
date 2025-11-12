package ca.uottawa.seg.otams;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public enum RegistrationStatus
{
    PENDING(RegistrationPendingPageActivity.class),
    REJECTED(RegistrationRejectedPageActivity.class),
    APPROVED(null);

    private final Class<? extends AppCompatActivity> classObj;

    RegistrationStatus(Class<? extends AppCompatActivity> registrationPending) {
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
