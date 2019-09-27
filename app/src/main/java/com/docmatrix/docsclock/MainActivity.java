package com.docmatrix.docsclock;

import android.app.TimePickerDialog;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {
    private Date previous_tick = new Date();
    private final int image_interval = 5;
    private final int[] images = {R.drawable.space_1, R.drawable.space_2, R.drawable.space_3};
    private int current_image = 0;

    private Date alarm_time = new Date();
    private boolean alarm_enabled = false;

    final SimpleDateFormat time_fmt = new SimpleDateFormat("HH:mm", Locale.UK);
    final SimpleDateFormat date_fmt = new SimpleDateFormat("EEEE, MMM dd", Locale.UK);

    ImageView img_brightness_overlay = null;
    ImageView img_background = null;
    ImageButton btn_alarm = null;
    ImageButton btn_brightness = null;
    Button btn_disable_alarm = null;
    Button btn_snooze = null;
    TextView txt_alarm = null;

    private boolean dimmed = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        time_fmt.setTimeZone(TimeZone.getTimeZone(getString(R.string.app_timezone)));
        date_fmt.setTimeZone(TimeZone.getTimeZone(getString(R.string.app_timezone)));

        btn_alarm = findViewById(R.id.btn_alarm);
        btn_brightness = findViewById(R.id.btn_brightness);
        btn_disable_alarm = findViewById(R.id.btn_disable_alarm);
        btn_snooze = findViewById(R.id.btn_snooze);
        txt_alarm = findViewById(R.id.txt_alarm);

        img_brightness_overlay = findViewById(R.id.img_brightness_overlay);
        img_background = findViewById(R.id.img_background);

        btn_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (alarm_enabled) {
                    alarm_enabled = false;
                    btn_alarm.setColorFilter(Color.argb(100, 255, 255, 255));
                    txt_alarm.setVisibility(View.INVISIBLE);
                }
                else {
                    final Calendar cldr = Calendar.getInstance();
                    cldr.setTimeZone(TimeZone.getTimeZone(getString(R.string.app_timezone)));
                    cldr.setTime(alarm_time);
                    int hour = cldr.get(Calendar.HOUR_OF_DAY);
                    int minutes = cldr.get(Calendar.MINUTE);

                    TimePickerDialog picker = new TimePickerDialog(MainActivity.this,
                            TimePickerDialog.THEME_HOLO_DARK,
                            new TimePickerDialog.OnTimeSetListener() {
                                @Override
                                public void onTimeSet(TimePicker tp, int sHour, int sMinute) {
                                    Date now = new Date();

                                    cldr.setTime(now);
                                    cldr.set(Calendar.HOUR_OF_DAY, sHour);
                                    cldr.set(Calendar.MINUTE, sMinute);
                                    cldr.set(Calendar.SECOND, 0);


                                    if (cldr.getTime().getTime() - now.getTime() < 0) {
                                        cldr.add(Calendar.DATE, 1);
                                    }
                                    alarm_time = cldr.getTime();
                                    alarm_enabled = true;
                                    btn_alarm.setColorFilter(Color.argb(255, 255, 255, 255));
                                    txt_alarm.setVisibility(View.VISIBLE);
                                    txt_alarm.setText(time_fmt.format(alarm_time));

                                    Log.i("MainActivity:setAlarm", alarm_time.toString());
                                }
                            }, hour, minutes, true);
                    picker.show();
                }
            }
        });

        btn_disable_alarm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                disable_alarm();
            }
        });

        btn_snooze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cldr = Calendar.getInstance();
                cldr.setTimeZone(TimeZone.getTimeZone(getString(R.string.app_timezone)));
                cldr.setTime(alarm_time);
                cldr.add(Calendar.MINUTE, 10);
                disable_alarm();
                set_alarm(cldr.getTime());
            }
        });

        btn_brightness.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (dimmed) {
                    undim();
                } else {
                    dim();
                }
            }
        });
    }

    private void disable_alarm() {
        alarm_enabled = false;
        btn_alarm.setColorFilter(Color.argb(100, 255, 255, 255));
        txt_alarm.setVisibility(View.INVISIBLE);
        btn_disable_alarm.setVisibility(View.INVISIBLE);
        btn_snooze.setVisibility(View.INVISIBLE);
    }

    private void set_alarm(Date alarm_time) {
        alarm_enabled = true;
        btn_alarm.setColorFilter(Color.argb(255, 255, 255, 255));
        txt_alarm.setVisibility(View.VISIBLE);
        txt_alarm.setText(time_fmt.format(alarm_time));

        this.alarm_time = alarm_time;

        Log.i("MainActivity:setAlarm", alarm_time.toString());
    }

    private void fire_alarm() {
        btn_disable_alarm.setVisibility(View.VISIBLE);
        btn_snooze.setVisibility(View.VISIBLE);
        undim();
    }

    private void dim() {
        img_brightness_overlay.setAlpha(0.8f);
        img_background.setVisibility(View.INVISIBLE);
        dimmed = true;
    }

    private void undim() {
        img_brightness_overlay.setAlpha(0.0f);
        img_background.setVisibility(View.VISIBLE);
        dimmed = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateTime();
    }

    public void updateTime() {
        final TextView txt_clock_time = findViewById(R.id.txt_clock);
        final TextView txt_clock_date = findViewById(R.id.txt_date);
        final Handler handler = new Handler();
        final Random randomn = new Random();

        Runnable updater = new Runnable() {
            @Override
            public void run() {
                Date now = new Date();

                if ((now.getTime() - previous_tick.getTime()) / 1000 > image_interval) {
                    previous_tick = now;
                    ImageView img_view = findViewById(R.id.img_background);
                    animate(img_view, Math.abs(randomn.nextInt() % images.length));
                }

                txt_clock_time.setText(time_fmt.format(now));
                txt_clock_date.setText(date_fmt.format(now));

                checkAlarm();

                handler.postDelayed(this, 1000);
            }
        };

        updater.run();
    }

    private void animate(final ImageView imageView, final int nextImageIndex) {
        Drawable[] layers = new Drawable[2];
        layers[0] = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(),images[current_image]));
        layers[1] = new BitmapDrawable(getResources(), BitmapFactory.decodeResource(getResources(), images[nextImageIndex]));

        current_image = nextImageIndex;

        TransitionDrawable transitionDrawable = new TransitionDrawable(layers);
        imageView.setImageDrawable(transitionDrawable);
        transitionDrawable.startTransition(1000);
    }

    private void checkAlarm() {
        if (alarm_enabled) {
            final Calendar cldr = Calendar.getInstance();
            cldr.setTimeZone(TimeZone.getTimeZone(getString(R.string.app_timezone)));
            cldr.setTime(alarm_time);
            Date now = new Date();
            if (now.getTime() > cldr.getTime().getTime()) {
                fire_alarm();
            }
        }
    }
}
