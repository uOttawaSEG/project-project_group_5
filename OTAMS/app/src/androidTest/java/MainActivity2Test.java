import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

import org.junit.Rule;
import org.junit.Test;

import ca.uottawa.seg.otams.MainActivity;
import ca.uottawa.seg.otams.R;

public class MainActivity2Test {
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityTestRule = new
            ActivityScenarioRule<>(MainActivity.class);
    @Test
    public void emailIsInvalid() {
        onView(withId(R.id.username_input)).perform(typeText("email@"), closeSoftKeyboard());
        onView(withId(R.id.password_input)).perform(typeText("password"), closeSoftKeyboard());
        onView(withId(R.id.login_btn)).perform(click());
        onView(withText("Invalid Email")).check(matches(isDisplayed()));
    }
}

