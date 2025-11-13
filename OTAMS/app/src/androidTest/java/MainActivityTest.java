import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.test.annotation.UiThreadTest;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import android.app.Activity;
import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ca.uottawa.seg.otams.MainActivity;
import ca.uottawa.seg.otams.R;

public class MainActivityTest {
    @Rule
    public ActivityScenarioRule<MainActivity> mActivityTestRule = new ActivityScenarioRule<MainActivity>(MainActivity.class);
    private MainActivity mActivity = null;
    private TextView text;

    @Before
    public void setUp() throws Exception {
        //mActivity = mActivityTestRule.getActivity();

        mActivityTestRule.getScenario().onActivity(activity -> {
            mActivity = activity;
        });
    }

    @Test
    @UiThreadTest
    public void checkFirstName() throws Exception {
        assertNotNull(mActivity.findViewById(R.id.username_input));
        text = mActivity.findViewById(R.id.username_input);
        text.setText("user1");
        String name = text.getText().toString();
        assertNotEquals("user", name);
    }
}