package plusequalsto.com.plusequalstoapp;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.core.view.MenuItemCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView notificationCount;
    int pendingnotificationCount = 0;

    SharedPreference sharedPreference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPreference = new SharedPreference(this);
        if (sharedPreference.getThemeModeState() == true) {
            setTheme(R.style.AppNightTheme);
        } else setTheme(R.style.AppDayTheme);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainer, new HomeFragment()).commit();

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu, menu);
        final MenuItem menuItem = menu.findItem(R.id.action_notifications);
        View actionView = MenuItemCompat.getActionView(menuItem);
        notificationCount = (TextView) actionView.findViewById(R.id.notification_badge);
        setupBadge();
        actionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOptionsItemSelected(menuItem);
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_notifications) {
            Intent notificationsIntent = new Intent(this, NotificationActivity.class);
            this.startActivity(notificationsIntent);
            Toast.makeText(this, pendingnotificationCount + " Notification(s)", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            this.startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver((mMessageReceiver),
                new IntentFilter("NotificationCount")
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pendingnotificationCount = intent.getExtras().getInt("Count");
            setupBadge();
        }
    };

    private void setupBadge() {
        Log.d("MainActivity", "Message Notification Body: " + pendingnotificationCount);
        if (notificationCount != null) {
            if (pendingnotificationCount == 0) {
                if (notificationCount.getVisibility() != View.GONE) {
                    notificationCount.setVisibility(View.GONE);
                }
            } else {
                notificationCount.setText(String.valueOf(Math.min(pendingnotificationCount, 99)));
                if (notificationCount.getVisibility() != View.VISIBLE) {
                    notificationCount.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    fragmentTransaction.replace(R.id.fragmentContainer, new HomeFragment()).commit();
                    return true;
                case R.id.navigation_dashboard:
                    fragmentTransaction.replace(R.id.fragmentContainer, new TrendingFragment()).commit();
                    return true;
                case R.id.navigation_notifications:
                    fragmentTransaction.replace(R.id.fragmentContainer, new CategoriesFragment()).commit();
                    return true;
            }
            return false;
        }
    };
}
