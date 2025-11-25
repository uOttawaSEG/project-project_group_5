package ca.uottawa.seg.otams;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import android.widget.TextView;
import static org.junit.Assert.*;

import androidx.test.ext.junit.rules.ActivityScenarioRule;

public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityTestRule = new ActivityScenarioRule<MainActivity>(MainActivity.class);
    private MainActivity mActivity = null;
    private TextView text;

    @Before
    public void setUp() throws Exception {
        //User the scenario to access the activity instance
        mActivityTestRule.getScenario().onActivity(activity -> {
            mActivity = activity;
        });
    }

    @Test
    public void checkFirstName() throws Exception {
        assertNotNull(mActivity.findViewById(R.id.username_input));
        text = mActivity.findViewById(R.id.username_input);
        text.setText("user1");
        String name = text.getText().toString();
        assertNotEquals("user", name);

    }
}