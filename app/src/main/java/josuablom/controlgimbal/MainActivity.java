package josuablom.controlgimbal;

import com.google.android.glass.app.Card;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.view.WindowUtils;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;

//import android.view.GestureDetector;

//Added instead of above import
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
public class MainActivity extends Activity {

    private GestureDetector mGestureDetector;
    private CardScrollView mCardScroller;
    private View mView;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().requestFeature(WindowUtils.FEATURE_VOICE_COMMANDS);
        mView = buildView();
        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }
            @Override
            public Object getItem(int position) {
                return mView;
            }
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }
            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });

        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                openOptionsMenu();
            }
        });

        //mGestureDetector = createGestureDetector(this);
        setContentView(mCardScroller);
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu){
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId ==  Window.FEATURE_OPTIONS_PANEL) {
            getMenuInflater().inflate(R.menu.main, menu);
            return true;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }


    private View buildView() {
        Card card = new Card(this);
        card.setText(R.string.app_name);
        card.addImage(R.drawable.simera_logo);
        return card.getView();
    }


    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        if (featureId == WindowUtils.FEATURE_VOICE_COMMANDS || featureId ==  Window.FEATURE_OPTIONS_PANEL) {
            switch (item.getItemId()) {
                case R.id.control_gimbal:
                    controlGimbal();
                    break;

                case R.id.home_gimbal:
                    homeGimbal();
                    break;

                case R.id.reset_gimbal:
                    resetGimbal();
                    break;
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }


    public void controlGimbal()
    {
        Intent resultsIntent = new Intent(this, SensorActivity.class);
        startActivity(resultsIntent);

    }

    public void resetGimbal()
    {
        Intent resetIntent = new Intent(this, ResetActivity.class);
        startActivity(resetIntent);
    }

    public void homeGimbal()
    {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        startActivity(homeIntent);
    }

}
